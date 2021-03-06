import validators, requests, re
import apikey, url, wifi, file
from io import BytesIO

class Analyser:
    def __init__(self, text, raw):
        self.text = text
        self.raw = raw

        self.file_class = None
        self.wifi_class = None
        self.url_class = None
        self.hexdump = bytes(raw).hex(" ")

    def get_hexdump(self):
        return self.hexdump

# --------------------------------------------------------
    
    def get_url_analysis(self):
        headers = {"Accept": "application/json",
			       "x-apikey": apikey.apikey()}
        status = None

        if self.url_class: # Check if a url has been scanned
            id = self.url_class.get_ID()
        else:
            return 3 # If not then exit

        VT_url = "https://www.virustotal.com/api/v3/analyses/" + id

        try:
            response = requests.request("GET", VT_url, headers=headers)
        except requests.ConnectTimeout as timeout:
            print(timeout)
            return 1
        
        if response:
            data = response.json()
            status = data["data"]["attributes"]["status"]
		
        if status == "completed":
            analysis = data["data"]["attributes"]["results"]
            if analysis:
                for scan in analysis.values():
                    if scan["category"] == "malicious" or scan["category"] == "suspicious":
                        self.url_class.set_reason(scan["result"])
                        break

            #print(data["data"]["attributes"]["stats"])
            self.url_class.set_harmless(data["data"]["attributes"]["stats"]["harmless"])
            self.url_class.set_malicious(data["data"]["attributes"]["stats"]["malicious"])
            self.url_class.set_suspicious(data["data"]["attributes"]["stats"]["suspicious"])
            self.url_class.generate_report() # Generate report
            return self.url_class.get_results() # Return the report on success
        elif status == "queued":
            return 2
        else:
            return 3

    def upload_url_for_scanning(self, address):
        headers = {"Accept": "application/json", 
			       "Content-Type": "application/x-www-form-urlencoded",
			       "x-apikey": apikey.apikey()}

        VT_url = "https://www.virustotal.com/api/v3/urls"
        try:
            response = requests.request("POST", VT_url, data="url=" + address, headers=headers)
        except requests.ConnectTimeout as timeout:
            print(timeout)
            return "Unable to submit URL for analysis. Try again."

        if response:
            data = response.json()
            report_id = data["data"]["id"]
            #print(f'The ID of the scan is: {report_id}')
            self.url_class = url.URL(report_id, address)
            return "url"
        return f'{address} is an unrecognised URL'
    
# --------------------------------------------------------
    
    def get_wifi_analysis(self):
        return self.wifi_class.get_report()

    def wifi_scanner(self, data):
        self.wifi_class = wifi.Wifi()

        array = re.findall("(.+?):((?:[^\\;]|\\.)*);", data[5:])
        print(array)

        for i in array:
            if i[0] == "S":
                self.wifi_class.set_SSID(i[1])
            elif i[0] == "T":
                self.wifi_class.set_authentication(i[1])
            elif i[0] == "P":
                self.wifi_class.set_password(i[1])
            elif i[0] == "H":
                if i[1].lower() == "true":
                    self.wifi_class.set_hidden()

# --------------------------------------------------------

    def get_file_analysis(self):
        if self.file_class: # Check if a file has been scanned
            id = self.file_class.get_ID()
            #print(id)
        else:
            return 3 # If not then exit

        headers = {"Accept": "application/json",
                   "x-apikey": apikey.apikey()}
        status = None

        VT_url = "https://www.virustotal.com/api/v3/files/" + id

        try:
            response = requests.request("GET", VT_url, headers=headers)
        except requests.ConnectTimeout as timeout:
            print(timeout)
            return 1

        if response:
            data = response.json()
            status = data["data"]["attributes"]["last_analysis_results"]
		
        if status:
            for scan in status.values():
                if scan["result"]:
                    self.file_class.set_reason(scan["result"])
                    print(scan["result"])
                    break

            print(data["data"]["attributes"]["last_analysis_stats"])
            self.file_class.set_harmless(data["data"]["attributes"]["last_analysis_stats"]["harmless"])
            self.file_class.set_malicious(data["data"]["attributes"]["last_analysis_stats"]["malicious"])
            self.file_class.set_suspicious(data["data"]["attributes"]["last_analysis_stats"]["suspicious"])
            print(self.file_class.get_report())
            return self.file_class.get_report() # Return report
        elif not status:
            return 2
        else:
            return 3

    def upload_file_for_scanning(self, contents):
        headers = {"x-apikey": apikey.apikey()}

        VT_url = 'https://www.virustotal.com/api/v3/files'

        data_file = BytesIO(bytes(contents))
        print(data_file.read())
        data_file.seek(0)
        files = {'file': ('file.exe', data_file)}

        try:
            response = requests.post(VT_url, headers=headers, files=files)
        except requests.ConnectTimeout as timeout:
            print(timeout)
            return "Unable to submit file for analysis. Try again."

        if response:
            data = response.json()
            report_id = data["data"]["id"]
            self.file_class = file.File(report_id)
            return "file"
        return f'{contents} is an unrecognised file'

# --------------------------------------------------------
    
    def analyser(self):
        print(f'The QR Code contains: {self.text}')
        print(f'The raw data is: {self.raw}')
        print(f'Hexdump:\n{self.hexdump}')

        valid_url = validators.url(self.text)
        if valid_url:
            #print(f'URL Found...')
            self.url_class = None
            return self.upload_url_for_scanning(self.text)

        valid_wifi = re.search("^WIFI:((?:.+?:(?:[^\\;]|\\.)*;)+);?$", self.text)
        if valid_wifi:
            #print(f'Wi-Fi Network Found...')
            self.wifi_class = None
            self.wifi_scanner(self.text)
            return "wifi"

        if not valid_url or not valid_wifi:
            #print(f'Generic file upload')
            self.file_class = None
            return self.upload_file_for_scanning(self.raw)

        return 0

    # def __init__(self, text, raw):
    #     self.text = text
    #     self.raw = raw
    #     self.hexdump = bytes(raw).hex(" ")

    # def analyser(self):
    #     print(f'The QR Code contains: {self.text}')
    #     print(f'The raw data is: {self.raw}')
    #     print(f'Hexdump:\n{self.hexdump}')

    #     valid_url = validators.url(self.text)
    #     if valid_url:
    #         print(f'URL Found...')
    #         return "url"
        

    #     valid_wifi = re.search("^WIFI:((?:.+?:(?:[^\\;]|\\.)*;)+);?$", self.text)
    #     if valid_wifi:
    #         print(f'Wi-Fi Network Found...')
    #         return "wifi"

    #     if not valid_url or not valid_wifi:
    #         print(f'Generic file upload')
    #     print(f'Failed to detect a file')

    #     return 0




#if __name__ == "__main__":
#    a = Analyser()
#    a.analyser("https://google.com")
