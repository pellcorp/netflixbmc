A very early app to allow using the netflix website and 'cast' to kodi via netflixbmc plugin on kodi.  When clicking on a movie to play, the app will
 send via jsonrpc to a KODI instance running the netflixbmc app.

Take a look at https://github.com/pellcorp/netflixbmc-server for a python 'mock' of the relevant jsonrpc portions of Kodi to be able to use this
app without kodi.  The netflixbmc-server does not require any authentication details however
