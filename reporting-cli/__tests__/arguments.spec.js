let path = require('path');
let exec = require('child_process').exec;

describe('Command arguments tests', () => {
    
    test('url is undefined', async () => {
      let result = await cli(['', ''], '.');
      expect(result.stderr).toContain('Please specify URL');
    });

    test('auth is given but credentials are missing', async () => {
      let result = await cli(['-u', 'https://test.com', '-a', 'basic'], '.');
      expect(result.stderr).toContain('Please specify a valid username or password');      
    });

    test('report format is invalid', async () => {
      let result = await cli(['-f', 'txt'], '.');
      expect(result.stderr).toContain('option \'-f, --format <type>\' argument \'txt\' is invalid. Allowed choices are pdf, png.');      
    });
    
});

function cli(args, cwd) {
  return new Promise(resolve => { 
    exec(`node ${path.resolve('./src/index.js')} ${args.join(' ')}`,
    { cwd }, 
    (error, stdout, stderr) => { resolve({
    error,
    stdout,
    stderr })
  })
})}