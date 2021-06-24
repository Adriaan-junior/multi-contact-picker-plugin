import Foundation
import Capacitor
import Contacts
import ContactsUI

typealias JSObject = [String:Any]

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(ContactPlugin)
public class ContactPlugin: CAPPlugin, CNContactPickerDelegate {
    
    var vc: CNContactPickerViewController?
    var id: String?

    @objc func echo(_ call: CAPPluginCall) {
        let value = call.getString("value") ?? ""
        call.success([
            "value": value
        ])
    }
    
    @objc func getContacts(_ call: CAPPluginCall) {
            // You could filter based on the value passed to the function!
            
        let contactStore = CNContactStore();
            
            contactStore.requestAccess(for: .contacts) { (granted, error) in
                if let error = error {
                    print("failed to request access", error)
                    call.reject("access denied")
                    return
                }
                if granted {
                   do {
                    self.id = call.callbackId
                    call.save()
                    DispatchQueue.main.async {
                        self.vc = CNContactPickerViewController()
                        self.vc!.delegate = self
                        self.bridge.viewController.present(self.vc!, animated: true, completion: nil)
                    }
                   }
                } else {
                    print("access denied")
                    call.reject("access denied")
                }
            }
        }

    @objc func search(_ call: CAPPluginCall) {
        let value = call.getString("number") ?? "";
        print("Value --> " + value);
            
            let contactStore = CNContactStore()
            var contacts = [Any]()
            let keys = [
                    CNContactFormatter.descriptorForRequiredKeys(for: .fullName),
                            CNContactPhoneNumbersKey,
                            CNContactEmailAddressesKey,
                            CNContactIdentifierKey
                    ] as [Any]
            let request = CNContactFetchRequest(keysToFetch: keys as! [CNKeyDescriptor])
            
            contactStore.requestAccess(for: .contacts) { (granted, error) in
                if let error = error {
                    print("failed to request access", error)
                    call.reject("access denied")
                    return
                }
                if granted {
                   do {
                       try contactStore.enumerateContacts(with: request){
                               (contact, stop) in
                        contacts.append([
                            "id":contact.identifier,
                            "firstName": contact.givenName,
                            "lastName": contact.familyName,
                            "telephone": contact.phoneNumbers.map { $0.value.stringValue }
                        ]);
                       }
                       print(contacts)
                       call.success([
                           "results": contacts
                       ])
                   } catch {
                       print("unable to fetch contacts")
                       call.reject("Unable to fetch contacts")
                   }
                } else {
                    print("access denied")
                    call.reject("access denied")
                }
            }
        }
    
    
    func makeContact(_ contact: CNContact) -> JSObject {
            var res = JSObject()
            res["id"] = contact.identifier;
            res["displayName"] = contact.givenName + " " + contact.familyName;
            res["phoneNumber"] = contact.phoneNumbers.map { $0.value.stringValue }
            return res
        }

        public func contactPicker(_ picker: CNContactPickerViewController, didSelect contacts: [CNContact]) {
            picker.dismiss(animated: true, completion: nil)
            let call = self.bridge.getSavedCall(self.id!)
            if (call != nil) {
                var object = JSObject()
                object["results"] =  contacts.map { makeContact($0) }
                call?.success(object);
            }
        }

        public func contactPickerDidCancel(_ picker: CNContactPickerViewController) {
            picker.dismiss(animated: true, completion: nil)
        }
    
}
