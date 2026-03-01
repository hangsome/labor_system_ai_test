import { spawnSync } from 'node:child_process'
import fs from 'node:fs'
import path from 'node:path'

const argv = process.argv.slice(2)
const pathPatternArg = argv.find((item) => item.startsWith('--testPathPattern='))

const vitestArgs = ['run']

if (pathPatternArg) {
  const pattern = pathPatternArg.slice('--testPathPattern='.length).trim()
  if (pattern.length > 0) {
    const matchedFiles = findMatchedTestFiles(path.resolve('tests'), pattern)
    if (matchedFiles.length > 0) {
      vitestArgs.push(...matchedFiles)
    }
  }
}

for (const arg of argv) {
  if (!arg.startsWith('--testPathPattern=')) {
    vitestArgs.push(arg)
  }
}

const command = process.platform === 'win32' ? 'npx' : 'npx'
const result = spawnSync(command, ['vitest', ...vitestArgs], {
  stdio: 'inherit',
  shell: process.platform === 'win32',
})

if (result.error) {
  console.error(result.error.message)
  process.exit(1)
}

process.exit(result.status ?? 1)

function findMatchedTestFiles(rootDir, keyword) {
  if (!fs.existsSync(rootDir)) {
    return []
  }
  const results = []
  const stack = [rootDir]
  while (stack.length > 0) {
    const currentDir = stack.pop()
    if (!currentDir) {
      continue
    }
    const entries = fs.readdirSync(currentDir, { withFileTypes: true })
    for (const entry of entries) {
      const fullPath = path.join(currentDir, entry.name)
      if (entry.isDirectory()) {
        stack.push(fullPath)
        continue
      }
      if (!entry.name.endsWith('.spec.ts')) {
        continue
      }
      if (!entry.name.includes(keyword)) {
        continue
      }
      results.push(path.relative(process.cwd(), fullPath).replaceAll('\\', '/'))
    }
  }
  return results
}
