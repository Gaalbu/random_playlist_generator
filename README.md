# random_playlist_generator
This is a project made for an academic project, that consists of a generator of random playlists of random genres

## Rodando localmente

```bash
cp .env.example .env   # preencha ao menos YOUTUBE_API_KEY
docker compose up --build -d
```
Frontend em `http://localhost:8081`, backend em `http://localhost:8080` (o nginx do frontend já faz proxy de `/api`, `/oauth2` e `/login`). O `restart: unless-stopped` do `docker-compose.yml` mantém os containers de pé entre reboots (o Docker já sobe sozinho no boot), então depois do primeiro `up` a aplicação já fica disponível sempre.

Só `YOUTUBE_API_KEY` é obrigatória — sem ela nada busca música. `GOOGLE_CLIENT_ID`/`GOOGLE_CLIENT_SECRET` são **opcionais**: sem eles o app sobe normal e só o botão "Salvar no YT Music" fica desativado. Pra criar as credenciais do Google (gratuito, ~5 min): [Google Cloud Console](https://console.cloud.google.com/apis/credentials) → criar credencial OAuth 2.0 tipo "Web application" → adicione `http://localhost:8080/login/oauth2/code/google` como URI de redirecionamento → habilite a "YouTube Data API v3" no projeto.

## Deploy (Vercel + Render)

O backend é Spring Boot — não roda como função serverless da Vercel. A combinação prática é
**frontend na Vercel** + **backend no Render** (Docker, free tier), mantendo o CI/CD do
`.github/workflows/ci.yml` publicando as imagens no GHCR em paralelo.

1. **Backend no Render**: New → Blueprint → aponte pro repo (usa `render.yaml` na raiz).
   Preencha `YOUTUBE_API_KEY`, `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET` no painel — `FRONTEND_URL`
   fica pra depois (passo 3). Anote a URL gerada, ex. `https://playlist-generator-backend.onrender.com`.
2. No **Google Cloud Console**, adicione `https://<url-do-backend>/login/oauth2/code/google` como
   URI de redirecionamento autorizada do client OAuth2.
3. **Frontend na Vercel**: Import Project → selecione o repo → em *Root Directory* escolha `frontend`
   (o `frontend/vercel.json` cuida do resto) → defina a env var `VITE_API_BASE_URL` com a URL do
   backend do passo 1 → Deploy. Anote a URL final, ex. `https://playlist-generator.vercel.app`.
4. Volte no Render e preencha `FRONTEND_URL` com a URL da Vercel do passo 3, depois faça redeploy
   do backend.

Notas:
- O plano free do Render hiberna após inatividade — a primeira geração de playlist depois de um
  tempo parado pode demorar ~30-50s pra "acordar" o serviço.
- Frontend e backend ficam em domínios diferentes nesse cenário, então o backend roda com o
  profile `prod` (`SPRING_PROFILES_ACTIVE=prod`, já definido no `render.yaml`) pra usar cookie de
  sessão `SameSite=None; Secure`, necessário pro login OAuth2 funcionar entre origens.
- O login com Google é habilitado automaticamente quando `GOOGLE_CLIENT_ID` está preenchido — não
  precisa de nenhuma env var ou profile extra pra isso.
