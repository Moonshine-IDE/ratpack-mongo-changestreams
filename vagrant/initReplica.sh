#!/bin/bash
# set -e

VAGRANT_DIR=/vagrant/vagrant

sudo mkdir -p /mongodb/logs/
sudo mkdir -p /mongodb/data
sudo touch /mongodb/logs/mongod.log
sudo chown mongodb:mongodb -R /mongodb/

sudo mv /etc/mongod.conf /etc/mongod.conf.bk
sudo cp $VAGRANT_DIR/mongod.conf /etc/mongod.conf
sudo cp $VAGRANT_DIR/hosts /etc/hosts
sudo sed -i "s/GUEST_IP/$1/g" /etc/hosts

sudo systemctl enable mongod.service
sudo systemctl start mongod

sleep 5 && mongosh --quiet <<EOF
rs.initiate();
exit;
EOF

sleep 5 && mongoimport --db test --collection grades --drop --file $VAGRANT_DIR/grades.json &>/dev/null
