# Ansible playbooks to provision and deploy cluster of CloudBees CI for Traditional #

Created by Phil Cherry, December 2020
## Introduction ##
These playbooks can be used to provision vm's, deploy and do some minimal configuration of CloudBees CI for Traditional in a cluster.  You may use them and adjust them to your needs, but are not supported by CloudBees.

**Note:  You will need your own license file for CloudBees CI.**

The playbooks currently use GCP for provisioning vm's, but this can easily by changed by modifying the relevant files (gcp-provision*).

There are several playbooks contained within:

- provision-and-deploy-all.yaml

  Performs all steps - provisions vm's, creates an external address for each, configures the firewall appropriately for each,  deploys and configures Operations Centre and Client Masters.  The inventory file has to be suitably prepared (see later) and is updated by the playbook.

  The playbooks currently provision Ubuntu 20.10 vm's.

- deploy-all.yaml

  Performs all the deploying and configuring steps for Operations Centre and Client Masters, but does not provision new vm's.  The inventory file must be fully prepared.

- deploy-cjoc.yaml

  Only deploys and configures Operations Centre.  The inventory file must be fully prepared.

- deploy-cm.yaml

  Only Client Masters are deployed and configured.  The inventory file must be fully prepared.

- gcp-provision-single-vm.yaml

  Can be used to provision a single vm with firewall and address correctly figured.  Vars are stored within the file, inventory file is not updated.

### CloudBees CI configuration ###

Currently the following configuration is performed:

Operations Center

- creates an admin user
- sets the Jenkins URL
- installs a set of plugins
- sets Jenkins Arg to skip the Jenkins setup wizard
- creates Client Master objects for each Client Master

Client Master

- creates an admin user
- sets the Jenkins URL
- installs a set of plugins
- sets Jenkins Arg to skip the Jenkins setup wizard
- sets Java Arg to connect the Client Master to Operations Centre

## Preparing Google Cloud Platform ##

In order for the playbooks to work properly, the following must be prepared in Google Cloud Platform:

- A GCP project already created
- A serviceaccount is needed for provisioning the infrastructure (vm's, firewall rules, address etc)
- A key json file should be downloaded (with Compute Admin rights), e.g. run:

```bash
gcloud iam service-accounts keys create ~/key.json --iam-account philsa@pcherry-ansible-test.iam.gserviceaccount.com
```

- Create SSH key pair so that we can connect to the vm's and carry out the installation (e.g. ssh-keygen -t rsa -b 4096 -C "ansible")
- Paste key in GCP console Compute Engine->Metadata->SSH Keys.  This makes the key available to all vm's created in that project.
- Enable Compute Engine API? Open the VM instances page in the UI?

## Preparing to run the playbooks ##

### 1. Set the variables ###

If you are going to use the provisioning playbook then, in gcp-provision-vm/vars/main.yaml you should set the following to match your GCP details:

- gcp_project (the GCP project to use)
- gcp_cred_file (the location of the service account json file created earlier)
- zone (the zone to use in GCP)
- region (the region to use in GCP)
- vm_user (the user to connect to the vm's using SSH)
- vm_ssh_key_file (the path to the SSH key file created earlier, e.g. ~/id_rsa)

In roles/cjoc/vars/main.yaml you should set the following:

- admin_user (the admin user to create in Operations Centre)
- admin_password (the password for the admin user)
- cloudbees_version (the version of Operations Center to install)
- license_file (the location of your CloudBees CI license file.  The easiest way to get this is to manually install Operations Center once, insert the license and copy the license file though it can be manually created too).
- plugins (the list of plugins and their versions to install)

In roles/cm/vars/main.yaml you should set the following:

- admin_user (the admin user to create in Operations Centre)
- admin_password (the password for the admin user)
- cloudbees_version (the version of Operations Center to install)
- plugins (the list of plugins and their versions to install)

### 2. Prepare the inventory file ###

If you are going to use the provisioning playbook then you need to create a basic inventory file that lists the cm's you want to create, e.g. if you want 2 client masters connected to operations center then it would look like this:

```ini
cjoc http_port=8888
[all_cm]
cm1 http_port=8080
cm2 http_port=8080
```

The playbook looks for an entry called cjoc so this must be there, it also uses the group 'all_cm'.  The names of the client masters can be anything you want.

The http_port is used to correctly configure the firewall for each vm.

If you are not going to use the provisioning playbook then you need to provide all the details, e.g.:

```ini
cjoc http_port=8888 ansible_host=12.345.678.90 ansible_connection=ssh ansible_user=phil ansible_ssh_private_key_file="~/id_rsa"
[all_cm]
cm1 http_port=8080 ansible_host=12.456.789.01 ansible_connection=ssh ansible_user=phil ansible_ssh_private_key_file="~/id_rsa"
cm2 http_port=8080 ansible_host=12.567.890.12 ansible_connection=ssh ansible_user=phil ansible_ssh_private_key_file="~/id_rsa"
```

### 3. Prepare the environment

Use ssh-agent and ssh-add so you don't need to enter the SSH key passphrase for every instance.  When running a full "provision-and-deploy-all.yaml" I kept getting issues with entering the password and this works around that problem:

```bash
eval "$(ssh-agent -s)"
ssh-add /Users/phil/Google Drive/Keys/GCE SSH Key/id_rsa
```

It may also be helpful to disable host key checking if you repeatably run the provisioning playbook, ie:

```bash
sudo vim ~/.ansible.cfg
```
and paste these lines in:

```bash
[defaults]
host_key_checking = False
```

## Running the playbooks ##

```bash
ansible-playbook provision-and-deploy-all.yaml -i inventory.txt
```

## Known issues ##
- The playbooks do not yet cope with upgrading an existing installation (issues like the groovy to create Client Master entries in OC fail as those entries already exist)
- SSH Authentication is requested throughout the execution of the playbook, and I had some issues with this (eg my password not always being accepeted).  See above for using ssh-agent and ssh-add to get around this.
- Ansible does not seem to reliably fire notify handlers so I added more fixed checks to ensure services are up and running before moving on.