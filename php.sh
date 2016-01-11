curl -L --output go-pear.phar http://pear.php.net/go-pear.phar &&
php go-pear.phar &&
mkdir -p protoc/bin

case `uname` in
  'Linux')
    PROTOC_SUFFIX="linux-x86_64"
    ;;
  'Darwin') 
    PROTOC_SUFFIX="osx-x86_64"
    ;;
esac

curl -L --output protoc/bin/protoc http://repo1.maven.org/maven2/com/google/protobuf/protoc/3.0.0-beta-2/protoc-3.0.0-beta-2-${PROTOC_SUFFIX}.exe &&
export PATH=$PATH:./protoc/bin &&
chmod 755 ./protoc/bin/protoc &&
protoc --version &&
curl -L --output Protobuf-1.0.tgz https://github.com/tayama0324/heroku-buildpack-protoc/raw/5c61435029e/opt/Protobuf-1.0.tgz &&
pear install Protobuf-1.0.tgz
