package org.openprovenance.prov.rdf;

import javax.xml.namespace.QName;

import org.openprovenance.prov.xml.NamespacePrefixMapper;
import org.openrdf.model.Resource;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.contextaware.ContextAwareRepository;

/** This class is a Sesame-based rdf graph builder. Ideally, it should
 * be abstracted in a generic interface and specific implementations that
 * are based on sesame/jena/other 
 */

public class SesameGraphBuilder implements GraphBuilder<Resource,LiteralImpl,org.openrdf.model.Statement> {

    final ContextAwareRepository manager;

    public SesameGraphBuilder(ContextAwareRepository manager) {
	this.manager = manager;
    }

    /* (non-Javadoc)
     * @see org.openprovenance.prov.rdf.GraphBuilder#assertStatement(org.openrdf.model.Statement)
     */
    @Override
    public void assertStatement(org.openrdf.model.Statement stmnt) {
	try {
	    if (currentContext==null) {
		manager.getConnection().add(stmnt);
	    } else {
		manager.getConnection().add(stmnt, currentContext);
	    }
	} catch (org.openrdf.repository.RepositoryException e) {
	}
    }

    /* (non-Javadoc)
     * @see org.openprovenance.prov.rdf.GraphBuilder#createDataProperty(org.openrdf.model.Resource, javax.xml.namespace.QName, org.openrdf.model.impl.LiteralImpl)
     */
    @Override
    public org.openrdf.model.Statement createDataProperty(org.openrdf.model.Resource r,
					    QName pred, 
					    LiteralImpl literalImpl) {
	return new StatementImpl(r, qnameToURI(pred), literalImpl);
    }

    /* (non-Javadoc)
     * @see org.openprovenance.prov.rdf.GraphBuilder#createDataProperty(javax.xml.namespace.QName, javax.xml.namespace.QName, org.openrdf.model.impl.LiteralImpl)
     */
    @Override
    public org.openrdf.model.Statement createDataProperty(QName subject, 
                                            QName pred,
					    LiteralImpl literalImpl) {
	return createDataProperty(qnameToResource(subject), pred, literalImpl);
    }

    /* (non-Javadoc)
     * @see org.openprovenance.prov.rdf.GraphBuilder#createObjectProperty(org.openrdf.model.Resource, javax.xml.namespace.QName, javax.xml.namespace.QName)
     */
    @Override
    public org.openrdf.model.Statement createObjectProperty(org.openrdf.model.Resource r,
					      QName pred, 
					      QName object) {
	return new StatementImpl(r, qnameToURI(pred), qnameToURI(object));
    }

    /* (non-Javadoc)
     * @see org.openprovenance.prov.rdf.GraphBuilder#createObjectProperty(javax.xml.namespace.QName, javax.xml.namespace.QName, javax.xml.namespace.QName)
     */
    @Override
    public  org.openrdf.model.Statement createObjectProperty(QName subject, 
                                              QName pred,
					      QName object) {
	return new StatementImpl(qnameToResource(subject), qnameToURI(pred),
				 qnameToResource(object));
    }

    /* (non-Javadoc)
     * @see org.openprovenance.prov.rdf.GraphBuilder#qnameToURI(javax.xml.namespace.QName)
     */
    @Override
    public URIImpl qnameToURI(QName qname) {
	if (qname.getNamespaceURI().equals(NamespacePrefixMapper.XSD_NS)) {
	    return new URIImpl(NamespacePrefixMapper.XSD_HASH_NS
		    + qname.getLocalPart());
	} else {
	    return new URIImpl(qname.getNamespaceURI() + qname.getLocalPart());

	}
    }

    /* (non-Javadoc)
     * @see org.openprovenance.prov.rdf.GraphBuilder#qnameToResource(javax.xml.namespace.QName)
     */
    @Override
    public Resource qnameToResource(QName qname) {
	if (qname.getNamespaceURI().equals(NamespacePrefixMapper.XSD_NS)) {
	    return new URIImpl(NamespacePrefixMapper.XSD_HASH_NS
		    + qname.getLocalPart());
	}
	if (isBlankName(qname)) {
	    return new BNodeImpl(qname.getLocalPart());
	} else {
	    return new URIImpl(qname.getNamespaceURI() + qname.getLocalPart());

	}
    }

    boolean isBlankName(QName name) {
	return name.getNamespaceURI().equals(NamespacePrefixMapper.TOOLBOX_NS)
		&& name.getPrefix().equals("_");
    }

    static int blankCounter = 0;

    /* (non-Javadoc)
     * @see org.openprovenance.prov.rdf.GraphBuilder#newBlankName()
     */
    @Override
    public QName newBlankName() {
	blankCounter++;
	return newToolboxQName("blank" + blankCounter);
    }

    public static QName newToolboxQName(String local) {
	return new QName(NamespacePrefixMapper.TOOLBOX_NS, local, "_");
    }

    public void setContext() {
	currentContext=null;
    }
    
    private Resource currentContext=null;
    
    public void setContext(Resource uri) {
	currentContext=uri;
	
    }

    @Override
    public LiteralImpl newLiteral(String value, QName type) {
	 return new LiteralImpl(value,
	                        qnameToURI(type));
    }
    @Override
    public LiteralImpl newLiteral(String value, String lang) {
	 return new LiteralImpl(value,lang);
    }

}
