google-grp-scraper is a crawler which takes google group URL and no-of-downloader threads in input

i.e. java -jar google-grp-scraper-0.0.1-SNAPSHOT.jar <URL_OF_YOUR_GROUP> <NO_OF_DOWNLOADER_THREADS> 

example:- java -jar google-grp-scraper-0.0.1-SNAPSHOT.jar 'https://groups.google.com/forum/#!forum/ibm.software.websphere.application-server' 20



OUTPUT:- After running above you will get output as below:
	1) Topics will be downloaded at (Download/<YOUR_GROUP_NAME>/Topics)
        2) Recovery file will be created at (Download/<YOUR_GROUP_NAME>/Recovery)




