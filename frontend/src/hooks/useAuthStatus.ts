import { useEffect, useState } from 'react'
import { api, type AuthStatus } from '../lib/api'

export function useAuthStatus() {
  const [status, setStatus] = useState<AuthStatus | null>(null)

  useEffect(() => {
    api.authStatus().then(setStatus).catch(() => setStatus({ authenticated: false, loginUrl: '/oauth2/authorization/google' }))
  }, [])

  return status
}
