import { beforeEach, describe, expect, it, vi } from 'vitest'
import { apiFetch } from './api'

describe('apiFetch', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
  })

  it('returns parsed JSON when the response is successful', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue({
        ok: true,
        status: 200,
        json: async () => ({ name: 'Ada' }),
      }),
    )

    await expect(apiFetch<{ name: string }>('/users')).resolves.toEqual({ name: 'Ada' })
  })

  it('throws when the response is not successful', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue({
        ok: false,
        status: 500,
        text: async () => 'server error',
      }),
    )

    await expect(apiFetch('/users')).rejects.toThrow('server error')
  })

  it('returns undefined for 204 responses', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue({
        ok: true,
        status: 204,
      }),
    )

    await expect(apiFetch('/users/1')).resolves.toBeUndefined()
  })
})
