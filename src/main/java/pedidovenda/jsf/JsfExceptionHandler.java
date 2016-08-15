package pedidovenda.jsf;

import java.io.IOException;
import java.util.Iterator;

import javax.faces.FacesException;
import javax.faces.application.ViewExpiredException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;
import javax.xml.crypto.dsig.spec.ExcC14NParameterSpec;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.LogFactoryImpl;

import pedidovenda.service.NegocioException;

public class JsfExceptionHandler extends ExceptionHandlerWrapper {

	private static Log log =LogFactory.getLog(JsfExceptionHandler.class);
	
	private ExceptionHandler wrapped;

	public JsfExceptionHandler(ExceptionHandler wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public ExceptionHandler getWrapped() {
		return this.wrapped;
	}

	@Override
	public void handle() throws FacesException {
		Iterator<ExceptionQueuedEvent> events = getUnhandledExceptionQueuedEvents()
				.iterator();

		while (events.hasNext()) {
			ExceptionQueuedEvent event = events.next();
			ExceptionQueuedEventContext context = (ExceptionQueuedEventContext) event
					.getSource();

			Throwable exception = context.getException();

			boolean handle = false;
			NegocioException negocioException = getNegocioExcepion(exception);

			try {
				if (exception instanceof ViewExpiredException) {
					handle = true;
					redirect("/");
				} else if (negocioException != null) {
					handle = true;
					FacesUtil.addErrorMessage(negocioException.getMessage());
				} else {
					handle = true;
					//Imprimindo erro e a causa do erro.
					log.error("Erro de Sistema: "+ exception.getMessage(), exception );
					redirect("/Erro.xhtml");
				}
			} finally {
				if (handle) {
					events.remove();
				}
			}
		}

		getWrapped().handle();
	}

	private NegocioException getNegocioExcepion(Throwable exception) {
		if (exception instanceof NegocioException) {
			return (NegocioException) exception;
		} else if (exception.getCause() != null) {
			return getNegocioExcepion(exception.getCause());
		}

		return null;

	}

	private void redirect(String page) {
		try {
			FacesContext facesContext = FacesContext.getCurrentInstance();
			ExternalContext externalContext = facesContext.getExternalContext();
			String contextPath = externalContext.getRequestContextPath();

			externalContext.redirect(contextPath + page);
			facesContext.responseComplete();
		} catch (IOException e) {
			throw new FacesException(e);
		}
	}
}