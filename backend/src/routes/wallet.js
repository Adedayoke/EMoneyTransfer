const express = require('express');
const router = express.Router();
const { Prisma } = require('@prisma/client');
const prisma = require('../db');
const authMiddleware = require('../middleware/auth');

// Generates a human-readable transaction reference: TRF-K9X2M-A1B2
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

    if (!wallet) {
      return res.status(404).json({ error: 'Wallet not found' });
    }

    res.json({ balance: parseFloat(wallet.balance.toString()).toFixed(2) });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// POST /api/wallet/transfer
router.post('/transfer', authMiddleware, async (req, res) => {
  try {
    const { recipient_email, amount } = req.body;
    const senderId = req.user.id;

    if (!recipient_email || amount === undefined || amount === null) {
      return res.status(400).json({ error: 'Recipient email and amount are required' });
    }

    const parsedAmount = parseFloat(amount);
    if (isNaN(parsedAmount) || parsedAmount <= 0) {
      return res.status(400).json({ error: 'Amount must be a positive number' });
    }

    if (parsedAmount < 1) {
      return res.status(400).json({ error: 'Minimum transfer amount is ₦1.00' });
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(recipient_email)) {
      return res.status(400).json({ error: 'Invalid recipient email address' });
    }

    const recipient = await prisma.user.findUnique({
      where: { email: recipient_email.toLowerCase().trim() }
    });

    if (!recipient) {
      return res.status(404).json({ error: 'No account found with that email address' });
    }

    if (recipient.id === senderId) {
      return res.status(400).json({ error: 'You cannot transfer to your own account' });
    }

    let newBalance;
    const reference = generateReference();

    // ACID: Serializable transaction prevents race conditions and double-spends
    await prisma.$transaction(async (tx) => {
      const senderWallet = await tx.wallet.findUnique({
        where: { userId: senderId }
      });

      const currentBalance = parseFloat(senderWallet.balance.toString());
      if (currentBalance < parsedAmount) {
        throw new Error('INSUFFICIENT_BALANCE');
      }

      const updatedSender = await tx.wallet.update({
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

      newBalance = parseFloat(updatedSender.balance.toString()).toFixed(2);
    }, { isolationLevel: Prisma.TransactionIsolationLevel.Serializable });

    res.json({
      message: 'Transfer successful',
      new_balance: newBalance,
      reference
    });
  } catch (err) {
    if (err.message === 'INSUFFICIENT_BALANCE') {
      return res.status(400).json({ error: 'Insufficient balance for this transfer' });
    }
    console.error(err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

module.exports = router;
