FROM staillansag/msr-dce-dev:0.0.6

EXPOSE 5555
EXPOSE 5543
EXPOSE 9999

USER sagadmin

ADD --chown=sagadmin . /opt/softwareag/IntegrationServer/packages/dcePersonnes

RUN chgrp -R 0 /opt/softwareag/IntegrationServer/packages/dcePersonnes && chmod -R g=u /opt/softwareag/IntegrationServer/packages/dcePersonnes
