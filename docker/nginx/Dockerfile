FROM nginx

COPY run.sh /

COPY html /usr/share/nginx/html
RUN rm /etc/nginx/conf.d/default.conf
COPY loadbalance.conf /etc/nginx/conf.d/loadbalance.conf

CMD ["./run.sh"]
