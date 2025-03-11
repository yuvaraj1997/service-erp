# service-erp

# To generate private key & public key

### Generate a 2048-bit private key
openssl genpkey -algorithm RSA -out private_key.pem -pkeyopt rsa_keygen_bits:2048

### Extract the public key from the private key
openssl rsa -pubout -in private_key.pem -out public_key.pem
