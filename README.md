# webservicesystagram
Versão simplificada do Web Service do Systagram para o Google API Engine

Teste

curl -v http://localhost:8080/webservicesystagram/rest/users
curl -v http://localhost:8080/webservicesystagram/rest/users/1

curl -v --header "Content-Type: application/json" --request POST --data '{"username":"novousuario", "password":"novousuario"}' http://localhost:8080/webservicesystagram/rest/users
