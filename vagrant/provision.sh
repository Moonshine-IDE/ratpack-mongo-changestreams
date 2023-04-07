#!/bin/bash

VAGRANT_DIR=/vagrant/vagrant
HOME_DIR=~/
HOME_BIN_DIR=$HOME_DIR/bin
MONGO_VERSION=$1
MONGO_COMPONENT_VERSION=$2

installPackage()
{
    local packages=$*
    echo "Installing $packages"
    sudo apt-get install -y $packages >/dev/null 2>&1
}

updatePackages()
{
    sudo add-apt-repository ppa:openjdk-r/ppa -y >/dev/null 2>&1
    sudo apt-get update >/dev/null 2>&1
}

installPackages()
{
    updatePackages
    installPackage git
    installPackage zip
    installPackage unzip
    installPackage make build-essential libssl-dev zlib1g-dev libbz2-dev libreadline-dev libsqlite3-dev wget curl llvm libncurses5-dev
}

createAndMoveToHomeBinDir()
{
    echo "Creating and moving to bin directory"
    mkdir $HOME_BIN_DIR
    cd $HOME_BIN_DIR
}

setDefaultJava()
{
    default_java_version=$1
    echo "Setting jdk $default_java_version globally"
    jenv global $default_java_version
}

installOpenjdk()
{
    version=$1
    echo "Installing openjdk-$version-jdk"
    sudo apt-get install -y openjdk-$version-jdk >/dev/null 2>&1;
    jenv add /usr/lib/jvm/java-$version-openjdk-amd64 >/dev/null 2>&1
}

installJenv()
{
    echo 'Installing jenv'
    git clone https://github.com/gcuisinier/jenv.git ~/.jenv >/dev/null 2>&1
    export PATH="$HOME/.jenv/bin:$PATH"
    eval "$(jenv init -)"
    jenv enable-plugin export >/dev/null 2>&1
}

installEnvManagers()
{
    installJenv
}

createBashrcAndBashProfile()
{
    echo "Creating .bashrc and .bash_profile"
    cat $VAGRANT_DIR/bashrc.template > $HOME_DIR/.bashrc
    source $HOME_DIR/.bashrc
    cat $VAGRANT_DIR/bash_profile.template > $HOME_DIR/.bash_profile
    source $HOME_DIR/.bash_profile
}

installMongo()
{
    VERSION=$1
    COMPONENT_VERSION=$2
    echo "Installing mongodb ${COMPONENT_VERSION}"
    sudo apt-get install gnupg
    wget -qO - "https://www.mongodb.org/static/pgp/server-${VERSION}.asc" | sudo apt-key add -
    echo "deb [ arch=amd64,arm64 ] https://repo.mongodb.org/apt/ubuntu bionic/mongodb-org/${VERSION} multiverse" | sudo tee "/etc/apt/sources.list.d/mongodb-org-${VERSION}.list"
    sudo apt-get update
    mongo_packages=("mongodb-org=${COMPONENT_VERSION}" "mongodb-org-server=${COMPONENT_VERSION}" "mongodb-org-shell=${COMPONENT_VERSION}" "mongodb-org-mongos=${COMPONENT_VERSION}" "mongodb-org-tools=${COMPONENT_VERSION}"  mongodb-mongosh)
    sudo apt-get install -y "${mongo_packages[@]}"

    echo "mongodb-org hold" | sudo dpkg --set-selections
    echo "mongodb-org-server hold" | sudo dpkg --set-selections
    echo "mongodb-org-shell hold" | sudo dpkg --set-selections
    echo "mongodb-org-mongos hold" | sudo dpkg --set-selections
    echo "mongodb-org-tools hold" | sudo dpkg --set-selections
}


provision() {
    createAndMoveToHomeBinDir
    installPackages
    installEnvManagers
    installOpenjdk "8"
    createBashrcAndBashProfile
    installMongo "$MONGO_VERSION" "$MONGO_COMPONENT_VERSION"
}

if [ ! -f "/var/vagrant_provision" ]; then
    sudo touch /var/vagrant_provision
    provision
else
    echo "Machine already provisioned. Run 'vagrant destroy' and 'vagrant up' to re-create."
fi
