module.exports = {
  rules: {
    'type-empty': [2, 'never'],
    'type-case': [2, 'always', 'lower-case'],
    'type-enum': [
      2,
      'always',
      ['feat', 'fix', 'refactor', 'perf', 'test', 'docs', 'style', 'build', 'ci', 'chore', 'revert'],
    ],
    'scope-case': [2, 'always', 'lower-case'],
    'scope-enum': [
      2,
      'always',
      ['gateway', 'api-1', 'api-2', 'postgres', 'redis', 'compose', 'ci', 'docs', 'deps', 'repo'],
    ],
    'subject-empty': [2, 'never'],
    'subject-case': [2, 'never', ['sentence-case', 'start-case', 'pascal-case', 'upper-case']],
    'subject-full-stop': [2, 'never', '.'],
    'header-max-length': [2, 'always', 72],
    'body-leading-blank': [2, 'always'],
    'body-max-line-length': [2, 'always', 100],
    'footer-empty': [2, 'always'],
  },
};
