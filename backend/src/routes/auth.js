const express = require('express');
const router = express.Router();
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');
const rateLimit = require('express-rate-limit');
const prisma = require('../db');

const loginLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 5,
  skipSuccessfulRequests: true,
  standardHeaders: true,
  legacyHeaders: false,
  message: { error: 'Too many login attempts. Please try again in 15 minutes.' }
});

// Generate a unique 10-digit account number
async function generateAccountNumber() {
  let accountNumber;
  let unique = false;
  while (!unique) {
    accountNumber = String(Math.floor(1000000000 + Math.random() * 9000000000));
    const existing = await prisma.user.findUnique({ where: { accountNumber } });
    unique = !existing;
  }
  return accountNumber;
}

function signToken(user) {
  return jwt.sign(
    { id: user.id, email: user.email },
    process.env.JWT_SECRET,
    { expiresIn: '15m' }
  );
}

// POST /api/auth/register
router.post('/register', async (req, res) => {
  try {
    const { full_name, email, password, pin } = req.body;

    if (!full_name || !email || !password || !pin) {
      return res.status(400).json({ error: 'All fields are required' });
    }
    if (full_name.trim().length < 2) {
      return res.status(400).json({ error: 'Full name must be at least 2 characters' });
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      return res.status(400).json({ error: 'Invalid email address' });
    }
    if (password.length < 6) {
      return res.status(400).json({ error: 'Password must be at least 6 characters' });
    }
    if (!/^\d{4}$/.test(pin)) {
      return res.status(400).json({ error: 'PIN must be exactly 4 digits' });
    }

    const existing = await prisma.user.findUnique({ where: { email } });
    if (existing) {
      return res.status(409).json({ error: 'An account with this email already exists' });
    }

    const [passwordHash, pinHash, accountNumber] = await Promise.all([
      bcrypt.hash(password, 10),
      bcrypt.hash(pin, 10),
      generateAccountNumber()
    ]);

    let newUser;
    await prisma.$transaction(async (tx) => {
      newUser = await tx.user.create({
        data: {
          fullName: full_name.trim(),
          email: email.toLowerCase().trim(),
          passwordHash,
          pinHash,
          accountNumber
        }
      });
      await tx.wallet.create({ data: { userId: newUser.id } });
    });

    const token = signToken(newUser);

    res.status(201).json({
      message: 'Account created successfully',
      token,
      user: {
        id: newUser.id,
        full_name: newUser.fullName,
        email: newUser.email,
        account_number: newUser.accountNumber
      }
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// POST /api/auth/login  (rate-limited)
router.post('/login', loginLimiter, async (req, res) => {
  try {
    const { email, password } = req.body;

    if (!email || !password) {
      return res.status(400).json({ error: 'Email and password are required' });
    }

    const user = await prisma.user.findUnique({
      where: { email: email.toLowerCase().trim() }
    });

    // Constant-time check — never reveal which field was wrong
    if (!user) {
      await bcrypt.compare(password, '$2b$10$invalidhashtopreventtimingattackXXXXXXXXXXXXXXXXXXXX');
      return res.status(401).json({ error: 'Invalid email or password' });
    }

    const valid = await bcrypt.compare(password, user.passwordHash);
    if (!valid) {
      return res.status(401).json({ error: 'Invalid email or password' });
    }

    const token = signToken(user);

    res.json({
      token,
      user: {
        id: user.id,
        full_name: user.fullName,
        email: user.email,
        account_number: user.accountNumber
      }
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Internal server error' });
  }
});

module.exports = router;
