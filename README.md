# ClaySDK Demo Application

This is a sample app that shows how to obtain a token for the SaltoKS's APIs 
and activate an Android device to be used to unlock a Salto lock via Mobile Key.

By [Salto KS](https://saltoks.com/).

### How do I get set up? ###

In order to be able to login to our APIs an integrator is supposed to receive an OpenId client configuration.
This configuration will be specific and unique for any different integrator.

Please replace the configuration values obtained for your client in the strings.xml file where missing.
By default the app will point to the Salto's Connect APIs to their accept environment

```xml
    <string name="client_id"></string>
    <string name="dkg_client_id"></string>
    <string name="appauth_redirect_scheme"></string>
    <string name="redirect_url"></string>
    <string name="logout_redirect_url"></string>
    <string name="identity_server_url">https://clp-accept-identityserver.my-clay.com</string>
    <string name="public_api_key">MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEFYDlLVhKz+qNQIBASs322cib/iwnnuSWczXSvU8GGYB6pgZgaCroCywHMPclFRehVsB+jYRJd6n4zkhDSGd5bQ==</string>
    <string name="user_api_url">https://clp-accept-user.my-clay.com</string>
```

## Author

* [ClaySolutions](https://github.com/ClaySolutions) ([Victor](https://github.com/victorlsn))

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details
