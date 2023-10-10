<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
 
    <xsl:output method="xml" indent="yes"/>
 
    <!-- Modèle racine pour la balise <Values> -->
    <xsl:template match="/Values">
        <personne>
            <!-- Utilise l'attribut "name" pour trouver l'ID et le place comme attribut de la balise <personne> -->
            <xsl:attribute name="id">
                <xsl:value-of select="value[@name='personne_id']"/>
            </xsl:attribute>
 
            <!-- Copie le contenu des éléments "value" en tant que nouveaux éléments -->
            <xsl:apply-templates select="value[@name!='personne_id']"/>
        </personne>
    </xsl:template>
 
    <!-- Modèle pour la balise <value> -->
    <xsl:template match="value">
        <xsl:element name="{@name}">
            <xsl:value-of select="."/>
        </xsl:element>
    </xsl:template>
 
</xsl:stylesheet>