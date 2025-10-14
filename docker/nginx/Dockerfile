FROM nginx:1.29.2

COPY /docker/nginx/nginx.prod.conf /etc/nginx/nginx.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]