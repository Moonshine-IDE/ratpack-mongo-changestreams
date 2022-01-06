# -*- mode: ruby -*-
# vi: set ft=ruby :

GUEST_IP="192.168.1.90"
Vagrant.configure(2) do |config|

  config.vm.box = "ubuntu/bionic64"

  #config.disksize.size = '50GB'
  #config.vm.synced_folder "~/Projects/Prominic", "/home/vagrant/projects"

  config.vm.provider "virtualbox" do |vb|
     vb.name = "mongo_vm"
     vb.memory = "2048"
   end

  # Setting up public network interface
  config.vm.network "public_network", ip: GUEST_IP, auto_config: true,
    :mac => "525400c042d9",
    :netmask => "255.255.255.0"

  config.vm.provision "shell", path: "vagrant/provision.sh", privileged: false

  config.vm.provision "shell", path: "vagrant/initReplica.sh", privileged: false, args: [GUEST_IP]

end
