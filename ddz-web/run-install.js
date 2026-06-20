const { spawn } = require('child_process');
const fs = require('fs');
const path = require('path');

const logStream = fs.createWriteStream(path.join(__dirname, 'install-node.log'));
const npmCmd = process.platform === 'win32' ? 'npm.cmd' : 'npm';

console.log('Starting npm install...');
logStream.write('Start at ' + new Date().toISOString() + '\n');

const child = spawn(npmCmd, ['install', '--no-audit', '--no-fund'], {
  cwd: __dirname,
  stdio: ['ignore', 'pipe', 'pipe'],
  shell: true
});

child.stdout.on('data', (data) => {
  process.stdout.write(data);
  logStream.write(data);
});

child.stderr.on('data', (data) => {
  process.stderr.write(data);
  logStream.write(data);
});

child.on('error', (err) => {
  console.error('Spawn error:', err);
  logStream.write('Error: ' + err.message + '\n');
});

child.on('close', (code) => {
  logStream.write('Exit code: ' + code + '\n');
  logStream.write('End at ' + new Date().toISOString() + '\n');
  logStream.end();
  console.log('npm install finished with code', code);
  process.exit(code);
});
