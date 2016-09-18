#! /bin/sh

mysql() {
	docker rm -f mysql || true
	docker run --name mysql -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=test -p3306:3306 -d mysql:5.7 --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci
}

if [ -z ${1} ]; then
	echo "No db name provided"
	echo "Provide one of:"
	echo -e "\tmysql"
else
	${1}
fi