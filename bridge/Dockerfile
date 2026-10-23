FROM node:24-alpine
RUN apk add --no-cache python3 py3-pip
WORKDIR /app/bridge
COPY bridge/package*.json ./
RUN npm ci --omit=dev --ignore-scripts
COPY tools/requirements-youtube.txt /app/tools/requirements-youtube.txt
RUN python3 -m venv /opt/ytdl && /opt/ytdl/bin/pip install --no-cache-dir -r /app/tools/requirements-youtube.txt
COPY bridge/search.mjs bridge/server.mjs bridge/youtube.mjs ./
COPY tools/export_youtube_dl.py /app/tools/export_youtube_dl.py
ENV YOUTUBE_DL_PYTHON=/opt/ytdl/bin/python
USER node
EXPOSE 3000
CMD ["node","server.mjs"]
