FROM nginx

COPY start-nginx.sh /

COPY usr/share/nginx/html /usr/share/nginx/html
COPY etc/nginx/nginx.conf /etc/nginx/nginx.conf
COPY etc/nginx/stream_conf.d /etc/nginx/stream_conf.d

CMD ["./start-nginx.sh"]
