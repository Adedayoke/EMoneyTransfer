const express = require('express');
const router = express.Router();
const bcrypt = require('bcrypt');
const { Prisma } = require('@prisma/client');
const prisma = require('../db');
const authMiddleware = require('../middleware/auth');

function generateReference() {
  const ts = Date.now().toString(36).toUpperCase().slice(-5);
  const rand = Math.random().toString(36).substr(2, 4).toUpperCase();
  return `TRF-${ts}-${rand}`;
}

// GET /api/wallet/balance
router.get('/balance', authMiddleware, async (req, res) => {
  try {
    const wallet = await prisma.wallet.findUnique({
      where: { userId: req.user.id }
    });
    if (!wallet) return res.status(404).json({ error: 'Wallet not found' });
    res.json({ balance: parseFloat(wallet.balance.toString()).toFixed(2) });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// POST /api/wallet/transfer
router.post('/transfer', authMiddleware, async (req, res) => {
  try {
    const { recipient_account, amount, pin } = req.body;
    const senderId = req.user.id;

    if (!recipient_account || amount == null || !pin) {
      return res.status(400).json({ error: 'Recipient account, amount, and PIN are required' });
    }
    if (!/^\d{10}$/.test(recipient_account)) {
      return res.status(400).json({ error: 'Recipient account must be a 10-digit number' });
    }

    const parsedAmount = parseFloat(amount);
    if (isNaN(parsedAmount) || parsedAmount < 1) {
      return res.status(400).json({ error: 'Minimum transfer amount is ₦1.00' });
    }

    // Verify sender PIN
    const sender = await prisma.user.findUnique({
      where: { id: senderId },
      select: { pinHash: true, accountNumber: true }
    });
    const pinValid = sender.pinHash ? await bcrypt.compare(pin, sender.pinHash) : false;
    if (!pinValid) {
      return res.status(401).json({ error: 'Incorrect PIN. Please try again.' });
    }

    if (sender.accountNumber === recipient_account) {
      return res.status(400).json({ error: 'You cannot transfer to your own account' });
    }

    const recipient = await prisma.user.findUnique({
      where: { accountNumber: recipient_account }
    });
    if (!recipient) {
      return res.status(404).json({ error: 'No account found with that account number' });
    }

    let newBalance;
    const reference = generateReference();

    await prisma.$transaction(async (tx) => {
      const senderWallet = await tx.wallet.findUnique({ where: { userId: senderId } });
      const currentBalance = parseFloat(senderWallet.balance.toString());
      if (currentBalance < parsedAmount) throw new Error('INSUFFICIENT_BALANCE');

      const updated = await tx.wallet.update({
        where: { userId: senderId },
        data: { balance: { decrement: parsedAmount } }
      });
      await tx.wallet.update({
        where: { userId: recipient.id },
        data: { balance: { increment: parsedAmount } }
      });
      await tx.transaction.create({
        data: { senderId, receiverId: recipient.id, amount: parsedAmount, reference }
      });

      newBalance = parseFloat(updated.balance.toString()).toFixed(2);
    }, { isolationLevel: Prisma.TransactionIsolationLevel.Serializable });

    res.json({ message: 'Transfer successful', new_balance: newBalance, reference });
  } catch (err) {
    if (err.message === 'INSUFFICIENT_BALANCE') {
      return res.status(400).json({ error: 'Insufficient balance for this transfer' });
    }
    console.error(err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

module.exports = router;
