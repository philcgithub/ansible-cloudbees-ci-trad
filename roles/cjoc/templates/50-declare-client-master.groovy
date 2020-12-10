// Imports
import com.cloudbees.opscenter.server.model.ClientMaster
import com.cloudbees.opscenter.server.model.ConnectedMaster
import com.cloudbees.opscenter.server.model.OperationsCenter
import com.cloudbees.opscenter.server.properties.ConnectedMasterLicenseServerProperty
import com.cloudbees.opscenter.server.config.ConnectedMasterWebSocketProperty
import jenkins.model.Jenkins

// Input parameters
def clientMasterName = "{{ item }}"
def clientMasterId = 1
def clientMasterGrantId = "fjs2ktwfgd"

// Create Client Master Declaration
ClientMaster cm = OperationsCenter.instance.createClientMaster(clientMasterName)

//Set Client Master properties
cm.setId(clientMasterId)
cm.setIdName(ConnectedMaster.createIdName(clientMasterId, clientMasterName))
cm.setGrantId(clientMasterGrantId)
// Set WebSocket to true for this Client Master
cm.properties.replace(new ConnectedMasterWebSocketProperty(true))
// Set the licensing strategy to its default by passing 'null'
cm.properties.replace(new ConnectedMasterLicenseServerProperty(null))
cm.save()

if (OperationsCenter.instance.getConnectedMasterByName(cm.idName)!=null){
    println "Created ClientMaster '${cm.name}' known as '${cm.idName}'"
    println "-DMASTER_INDEX=${cm.id}'"
    println "-DMASTER_NAME=${cm.name}'"
    println "-DMASTER_GRANT_ID=${cm.grantId}'"
} else {
    println "[ERROR:]" + clientMasterName + "has not been created in CJOC"
}
return