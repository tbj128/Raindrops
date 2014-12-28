Server
=========

#### Pre-requisites ####
1. PHP5+ Apache Server with MySQL Support

#### Installation ####
1. Ensure that the server supports `.htaccess` rewrite (ie. accessing `/admin` will point to `/admin.php`)
	See the '`.htaccess` Troubleshooting' section for possible solutions.
2. Edit the `config.php` file 
3. Set `777 permissions` on the `content` directory
4. Set `777 permissions` on the `messages` directory
5. Set `777 permissions` on the `upload` directory

##### `.htaccess` Troubleshooting #####
This project relies on `.htaccess` rewrite capabilities. Not all servers support the rewrite capabilities by default. 
Try the following solutions if the server does not read the `.htaccess` properly.

1. Create a new file called `rewrite.conf` in `/etc/apache2/mods-enabled` 
2. In the file put this line `LoadModule rewrite_module /usr/lib/apache2/modules/mod_rewrite.so`
3. Restart the server, `sudo service apache2 restart`

--- 
a2enmod rewrite

1. Open the file `/etc/apache2/sites-enabled/000-default.conf`
2. Add the following to the `VirtualHost`:
```
<Directory "/var/www">
  AllowOverride All
</Directory>
```
3. Restart the server, `sudo service apache2 restart`