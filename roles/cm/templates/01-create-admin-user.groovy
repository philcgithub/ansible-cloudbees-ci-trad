import jenkins.model.*
import hudson.security.*

def env = System.getenv()

def jenkins = Jenkins.getInstance()
if(!(jenkins.getSecurityRealm() instanceof HudsonPrivateSecurityRealm))
    jenkins.setSecurityRealm(new HudsonPrivateSecurityRealm(false))

def user = jenkins.getSecurityRealm().createAccount("{{ admin_user }}", "{{ admin_password }}")
user.save()

jenkins.save()