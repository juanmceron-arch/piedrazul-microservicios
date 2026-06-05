# Frontend web estático servido con Nginx
FROM nginx:alpine

COPY frontend-web/ /usr/share/nginx/html/
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 5500
