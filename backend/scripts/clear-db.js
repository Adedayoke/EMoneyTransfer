require('dotenv').config({ path: require('path').join(__dirname, '../.env') });
const prisma = require('../src/db');

async function clearDatabase() {
  console.log('Clearing database...');

  // Order matters — delete dependents before parents
  const txDeleted = await prisma.transaction.deleteMany({});
  const walDeleted = await prisma.wallet.deleteMany({});
  const usrDeleted = await prisma.user.deleteMany({});

  console.log(`  Transactions deleted: ${txDeleted.count}`);
  console.log(`  Wallets deleted:      ${walDeleted.count}`);
  console.log(`  Users deleted:        ${usrDeleted.count}`);
  console.log('Done. Database is empty — register fresh.');
}

clearDatabase()
  .catch((err) => { console.error(err); process.exit(1); })
  .finally(() => prisma.$disconnect());
