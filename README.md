# dccd-lib

This is used by the DCCD webapplication.
For documentation see the DCCD webapplication README.md

### Problem building RPM
If the build fails when trying to build an RPM package, with an error similar to this:

   ```
   RPM build execution returned: '1' executing '/bin/sh -c cd '/Users/paulboon/git/service/dccd/dccd-lib/target/rpm/dccd-lib/SPECS' && 'rpmbuild' '-bb' '--target' 'noarch-apple-mac os x' '--buildroot' '/Users/paulboon/git/service/dccd/dccd-lib/target/rpm/dccd-lib/buildroot' '--define' '_topdir /Users/paulboon/git/service/dccd/dccd-lib/target/rpm/dccd-lib' 'dccd-lib.spec'' -> [Help 1]
org.apache.maven.lifecycle.LifecycleExecutionException: Failed to execute goal org.codehaus.mojo:rpm-maven-plugin:2.1.3:rpm (generate-rpm) on project dccd-lib: RPM build execution returned: '1' executing '/bin/sh -c cd '/Users/paulboon/git/service/dccd/dccd-lib/target/rpm/dccd-lib/SPECS' && 'rpmbuild' '-bb' '--target' 'noarch-apple-mac os x' '--buildroot' '/Users/paulboon/git/service/dccd/dccd-lib/target/rpm/dccd-lib/buildroot' '--define' '_topdir /Users/paulboon/git/service/dccd/dccd-lib/target/rpm/dccd-lib' 'dccd-lib.spec''
   ```       
   then you may have to externally define one of the macros of the `rpmbuild` tool. Create or edit the file `~/.rpmmacros` and add the following line:
   ```
   %_tmppath      %{_topdir}/BUILD
   ```           
