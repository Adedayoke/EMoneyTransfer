const express = require('express');
const router = express.Router();
const prisma = require('../db');
const authMiddleware = require('../middleware/auth');

// GET /api/transactions/history
router.get('/history', authMiddleware, async (req, res) => {
  try {
    const userId = req.user.id;

    const transactions = await prisma.transaction.findMany({
      where: {
        OR: [{ senderId: userId }, { receiverId: userId }]
      },
      include: {
        sender: { select: { fullName: true, email: true } },
        receiver: { select: { fullName: true, email: true } }
      },
      orderBy: { createdAt: 'desc' }
    });

    const formatted = transactions.map((t) => ({
      id: t.id,
      amount: parseFloat(t.amount.toString()).toFixed(2),
      status: t.status,
      created_at: t.createdAt.toISOString(),
      sender_name: t.sender.fullName,
      sender_email: t.sender.email,
      receiver_name: t.receiver.fullName,
      receiver_email: t.receiver.email,
      direction: t.senderId === userId ? 'sent' : 'received'
    }));

    res.json({ transactions: formatted });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

module.exports = router;
