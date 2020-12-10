import jenkins.model.JenkinsLocationConfiguration
def jenkinsLocationConfiguration = JenkinsLocationConfiguration.get()
jenkinsLocationConfiguration.setUrl("http://{{ ansible_host }}:{{ http_port }}")