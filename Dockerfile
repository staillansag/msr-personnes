FROM staillansag/msr-dce-dev:latest

EXPOSE 5555
EXPOSE 5543
EXPOSE 9999

USER sagadmin

ADD --chown=sagadmin . /opt/softwareag/IntegrationServer/packages/WmPackageTemplate

RUN chgrp -R 0 /opt/softwareag/IntegrationServer/packages/WmPackageTemplate && chmod -R g=u /opt/softwareag/IntegrationServer/packages/WmPackageTemplate
