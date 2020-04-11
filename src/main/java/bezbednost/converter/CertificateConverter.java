package bezbednost.converter;

import bezbednost.dto.response.CertificateResponseDTO;

import java.security.cert.X509Certificate;
import java.util.HashMap;

@SuppressWarnings({"SpellCheckingInspection", "ConstantConditions"})
public class CertificateConverter {

    public static CertificateResponseDTO toCertificateResponseDTO(X509Certificate certificate){
        CertificateResponseDTO certificateResponseDTO = new CertificateResponseDTO();
        HashMap<String, String> certData = getDataFromCertificate(certificate.getSubjectDN().getName());
        HashMap<String, String> parentData = getDataFromCertificate(certificate.getIssuerDN().getName());
        certificateResponseDTO.setFirstName(certData.get("firstName"));
        certificateResponseDTO.setLastName(certData.get("lastName"));
        certificateResponseDTO.setCAemail(parentData.get("email"));
        certificateResponseDTO.setEmail(certData.get("email"));
        certificateResponseDTO.setExpireDate(certificate.getNotAfter());

        return certificateResponseDTO;
    }

    /**
     * @param subject the certificate subject name
     * @return retMap - HashMap with keys uid, firstName, lastName, email, country, org, orgUnit
     * */
    public static HashMap<String, String> getDataFromCertificate(String subject){
        HashMap<String, String> retMap = new HashMap<>();
        String[] subjectArray = subject.split(",");

        for (String str : subjectArray) {
            str = str.trim();
            if(str.contains("UID=")){
                int indexEq = str.indexOf("=");
                retMap.put("uid", str.substring(indexEq+1));
            }
            else if(str.contains("EMAILADDRESS=")){
                int indexEq = str.indexOf("=");
                retMap.put("email", str.substring(indexEq+1));
            }
            else if(str.contains("C=")){
                int indexEq = str.indexOf("=");
                retMap.put("country", str.substring(indexEq+1));
            }
            else if(str.contains("O=")){
                int indexEq = str.indexOf("=");
                retMap.put("org", str.substring(indexEq+1));
            }
            else if(str.contains("OU=")){
                int indexEq = str.indexOf("=");
                retMap.put("orgUnit", str.substring(indexEq+1));
            }
            else if(str.contains("GIVENNAME=")){
                int indexEq = str.indexOf("=");
                retMap.put("firstName", str.substring(indexEq+1));
            }
            else if(str.contains("SURNAME=")){
                int indexEq = str.indexOf("=");
                retMap.put("lastName", str.substring(indexEq+1));
            }
        }

        return retMap;
    }

}
