sudo docker run --rm -v "$(pwd)/usr/share/nginx/html:/usr/share/nginx/html/" -v "$(pwd)/etc/nginx/stream_conf.d:/etc/nginx/stream_conf.d/" --net test-network -p 8888:80 -p 1883:1883 --name test-nginx test-nginx