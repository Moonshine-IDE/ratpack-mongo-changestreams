#!/bin/bash

VAGRANT_DIR=/vagrant/vagrant
HOME_DIR=~/
HOME_BIN_DIR=$HOME_DIR/bin

download()
{
    local url=$2
    local file=$1
    echo "Downloading $file"
    wget --progress=dot $url >/dev/null 2>&1
}

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
    echo "Installing mongodb"
    sudo apt-get install gnupg
    wget -qO - https://www.mongodb.org/static/pgp/server-5.0.asc | sudo apt-key add -
    echo "deb [ arch=amd64,arm64 ] https://repo.mongodb.org/apt/ubuntu bionic/mongodb-org/5.0 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-5.0.list >/dev/null 2>&1
    sudo apt-get update
    sudo apt-get install -y mongodb-org

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
    installMongo
}

if [ ! -f "/var/vagrant_provision" ]; then
    sudo touch /var/vagrant_provision
    provision
else
    echo "Machine already provisioned. Run 'vagrant destroy' and 'vagrant up' to re-create."
fi
