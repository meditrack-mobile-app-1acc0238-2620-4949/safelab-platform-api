const fs = require('fs');
const path = require('path');
const root = path.join(__dirname, '..');
const from = path.join(root, 'data/db.seed.json');
const to = path.join(root, 'data/runtime/db.json');
fs.mkdirSync(path.dirname(to), { recursive: true });
fs.copyFileSync(from, to);
console.log('Datos demo restaurados. Deten JSON Server antes del reset.');
