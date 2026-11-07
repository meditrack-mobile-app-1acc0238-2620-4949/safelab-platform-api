const fs = require('node:fs');
const assert = require('node:assert/strict');
const data = JSON.parse(fs.readFileSync('data/db.json','utf8'));
const catalog = JSON.parse(fs.readFileSync('data/permission-catalog.json','utf8'));
const counts = {organizations:3,users:19,sensors:12,assets:10,subscriptions:3};
for (const [key,expected] of Object.entries(counts)) assert.equal(data[key].length,expected,`${key} count mismatch`);
const orgs = new Set(data.organizations.map(o=>o.id));
assert.equal(orgs.size,data.organizations.length);
assert.equal(new Set(data.users.map(u=>u.email.toLowerCase())).size,data.users.length);
for (const key of ['users','sensors','assets','alerts','incidents','reports','auditLogs','subscriptions','maintenanceRecords']) {
  for (const record of data[key]) if(record.organizationId != null) assert.ok(orgs.has(record.organizationId),`${key} orphan: ${record.id}`);
}
const permissions = catalog.groups.flatMap(group=>group.permissions);
assert.equal(permissions.length,21);
assert.equal(new Set(permissions).size,21);
for (const [role,values] of Object.entries(catalog.defaults)) for(const permission of values)
  assert.ok(permissions.includes(permission),`invalid permission ${role}/${permission}`);
for(const subscription of data.subscriptions) assert.ok(subscription.includedSeats>=10);
console.log('SafeLab v1 fixtures OK: 3 organizations, 19 users, 12 sensors, 10 assets, 21 permissions.');
