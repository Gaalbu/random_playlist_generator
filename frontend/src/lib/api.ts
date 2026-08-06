export type GenreOption = { id: string; label: string; keyword: string }
export type DecadeOption = { id: string; label: string; keyword: string }
export type RandomFiltersDto = { genre: GenreOption; decade: DecadeOption }
export type TrackDto = {
  videoId: string
  title: string
  channelTitle: string
  thumbnailUrl: string
}
export type PlaylistResultDto = {
  title: string
  genres: GenreOption[]
  decades: DecadeOption[]
  tracks: TrackDto[]
}
export type GenerateRequest = {
  genreIds: string[] | null
  decadeIds: string[] | null
  trackCount: number | null
}
export type SaveRequest = { title: string; videoIds: string[] }
export type SaveResultDto = { playlistId: string; youtubeMusicUrl: string }
export type AuthStatus = { authenticated: boolean; loginUrl: string }

export const API_BASE = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`${API_BASE}${path}`, {
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    ...init,
  })
  if (!res.ok) {
    const message = await res.text().catch(() => res.statusText)
    throw new Error(message || `Request failed: ${res.status}`)
  }
  return res.json() as Promise<T>
}

export const api = {
  genres: () => request<GenreOption[]>('/api/genres'),
  decades: () => request<DecadeOption[]>('/api/decades'),
  randomFilters: () => request<RandomFiltersDto>('/api/filters/random'),
  generate: (body: GenerateRequest) =>
    request<PlaylistResultDto>('/api/playlist/generate', {
      method: 'POST',
      body: JSON.stringify(body),
    }),
  authStatus: () => request<AuthStatus>('/api/auth/status'),
  save: (body: SaveRequest) =>
    request<SaveResultDto>('/api/playlist/save', {
      method: 'POST',
      body: JSON.stringify(body),
    }),
  loginUrl: (loginUrl: string) => `${API_BASE}${loginUrl}`,
}
