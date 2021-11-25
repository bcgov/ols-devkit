package com.revolsys.jmx;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jeometry.common.exception.Exceptions;
import org.jeometry.common.logging.Logs;

import com.revolsys.io.map.MapWriter;
import com.revolsys.record.schema.FieldDefinition;

/**
 * <p>
 * The JmxService provides a wrapper around JMX connections to one or more
 * servers. The service is configured with a list of server definitions which
 * includes a logical name for the server and a connection to the server. Each
 * server has a list of object names and labels and the list of attributes and
 * operations to be exposed on that server. The only servers, objects,
 * attributes and operations to be exposed are those in the configuration.
 */
public class JmxService {
  /** The JMX server configurations. */
  private final Map<String, Map<String, Object>> jmxServers = new LinkedHashMap<>();

  /**
   * Construct a new JmxService.
   *
   * @param jmxServers The list of JMX server configurations.
   */
  public JmxService(final List<Map<String, Object>> jmxServers) {
    for (final Map<String, Object> server : jmxServers) {
      final String name = (String)server.get("name");
      if (name == null) {
        Logs.error(this, "Missing name for JMX Server: " + server);
      }
      this.jmxServers.put(name, server);
    }
  }

  /**
   * Get the value for the attribute for an object on a server.
   *
   * @param mapWriter The writer to write the attributes to.
   * @param serverId The name of the server.
   * @param objectId The name of the object.
   * @param attributeId The name of the attribute.
   * @return The attribute value.
   */
  public Object getAttribute(final String serverId, final String objectId,
    final String attributeId) {
    if (hasAttribute(serverId, objectId, attributeId)) {
      final MBeanServerConnection connection = getConnection(serverId);
      try {
        final ObjectName objectName = getObjectName(serverId, objectId);
        final Object object = connection.getAttribute(objectName, attributeId);
        return object;
      } catch (final InstanceNotFoundException e) {
        return "Unavailable";
      } catch (final MalformedObjectNameException e) {
        throw new IllegalArgumentException("MBean name not valid " + serverId + " " + objectId);
      } catch (final AttributeNotFoundException e) {
        throw new IllegalArgumentException(
          "MBean attribute not found " + serverId + " " + objectId + "." + attributeId);
      } catch (final Throwable e) {
        return Exceptions.throwUncheckedException(e);
      }
    } else {
      throw new IllegalArgumentException(
        "Attribute not configured " + serverId + " " + objectId + "." + attributeId);
    }
  }

  /**
   * Get all the attributes names for an object on a server.
   *
   * @param serverId The name of the server.
   * @param objectId The name of the object.
   * @return The attribute names.
   */
  @SuppressWarnings("unchecked")
  public List<String> getAttributeNames(final String serverId, final String objectId) {
    final Map<String, Object> object = getObjectParams(serverId, objectId);
    final List<Map<String, Object>> attributes = (List<Map<String, Object>>)object
      .get("attributes");

    final List<String> fieldNames = new ArrayList<>();
    for (final Map<String, Object> field : attributes) {
      final String name = (String)field.get("attributeName");
      if (name != null) {
        fieldNames.add(name);
      }
    }
    return fieldNames;
  }

  /**
   * Get the values for all attributes for an object on a server.
   *
   * @param serverId The name of the server.
   * @param objectId The name of the object.
   * @return The attribute values.
   */
  public List<FieldDefinition> getAttributes(final String serverId, final String objectId) {
    final MBeanServerConnection connection = getConnection(serverId);
    try {
      final String[] attributeNames = getAttributeNames(serverId, objectId).toArray(new String[0]);
      final ObjectName objectName = getObjectName(serverId, objectId);
      final List<FieldDefinition> attributeValues = new ArrayList<>();
      for (final Object attribute : connection.getAttributes(objectName, attributeNames)) {
        attributeValues.add((FieldDefinition)attribute);
      }
      return attributeValues;
    } catch (final InstanceNotFoundException e) {
      throw new IllegalArgumentException("MBean not found " + serverId + " " + objectId);
    } catch (final MalformedObjectNameException e) {
      throw new IllegalArgumentException("MBean name not valid " + serverId + " " + objectId);
    } catch (final Throwable e) {
      return Exceptions.throwUncheckedException(e);
    }
  }

  /**
   * Get a connection to the specified serverId. Returns an
   * {@link IllegalArgumentException} if the host or connection could not be
   * found.
   *
   * @param serverId The name of the server.
   * @return The JMX connection.
   */
  private MBeanServerConnection getConnection(final String serverId) {
    final Map<String, Object> server = getServerParams(serverId);
    final MBeanServerConnection connection = (MBeanServerConnection)server.get("connection");
    if (connection == null) {
      throw new IllegalArgumentException("Connection for server " + serverId + " not found");
    } else {
      return connection;
    }
  }

  /**
   * Get the object name for object on a server. Throws an
   * IllegalArgumentException if the object does not have a configuration.
   *
   * @param serverId The name of the server.
   * @param objectId The name of the object.
   * @return The server parameters.
   * @throws MalformedObjectNameException If the name of the object was not
   *           valid.
   */
  private ObjectName getObjectName(final String serverId, final String objectId)
    throws MalformedObjectNameException {
    final Map<String, Object> object = getObjectParams(serverId, objectId);
    final String objectName = (String)object.get("objectName");
    return new ObjectName(objectName);
  }

  /**
   * Get the configuration parameters for all objects on a server. Throws an
   * IllegalArgumentException if there are no objects configured.
   *
   * @param serverId The name of the server.
   * @return The server parameters.
   */
  @SuppressWarnings("unchecked")
  private List<Map<String, Object>> getObjectParams(final String serverId) {
    final Map<String, Object> server = getServerParams(serverId);
    final List<Map<String, Object>> objects = (List<Map<String, Object>>)server.get("objects");
    if (objects == null) {
      throw new IllegalArgumentException(
        "Server " + serverId + " does not have an objects attribute");
    }
    return objects;
  }

  /**
   * Get the configuration parameters for an object on a server. Throws an
   * IllegalArgumentException if the object does not have a configuration.
   *
   * @param serverId The name of the server.
   * @param objectId The name of the object.
   * @return The server parameters.
   */
  private Map<String, Object> getObjectParams(final String serverId, final String objectId) {
    final List<Map<String, Object>> objectNames = getObjectParams(serverId);
    for (final Map<String, Object> object : objectNames) {
      final String objectLabel = (String)object.get("objectLabel");
      if (objectId.equals(objectLabel)) {
        return object;
      }
    }

    throw new IllegalArgumentException(
      "Object for server " + serverId + " " + objectId + " not found");
  }

  /**
   * Get the description for the operation for an object on a server.
   *
   * @param serverId The name of the server.
   * @param objectId The name of the object.
   * @param operationId The name of the operation.
   * @return The operations values.
   */
  public MBeanOperationInfo getOperation(final String serverId, final String objectId,
    final String operationId) {
    if (hasOperation(serverId, objectId, operationId)) {
      final MBeanServerConnection connection = getConnection(serverId);

      try {
        final ObjectName objectName = getObjectName(serverId, objectId);
        final MBeanInfo beanInfo = connection.getMBeanInfo(objectName);
        for (final MBeanOperationInfo operation : beanInfo.getOperations()) {
          final String name = operation.getName();
          if (operationId.equals(name)) {
            return operation;
          }
        }
        throw new IllegalArgumentException(
          "MBean Operation not found " + serverId + " " + objectId + "." + operationId);
      } catch (final InstanceNotFoundException e) {
        throw new IllegalArgumentException("MBean not found " + serverId + " " + objectId);
      } catch (final MalformedObjectNameException e) {
        throw new IllegalArgumentException("MBean name not valid " + serverId + " " + objectId);
      } catch (final Throwable e) {
        return Exceptions.throwUncheckedException(e);
      }
    } else {
      throw new IllegalArgumentException(
        "Operation not configured " + serverId + " " + objectId + "." + operationId);
    }
  }

  /**
   * Get all the operations names for an object on a server.
   *
   * @param serverId The name of the server.
   * @param objectId The name of the object.
   * @return The attribute names.
   */
  @SuppressWarnings("unchecked")
  public List<String> getOperationNames(final String serverId, final String objectId) {
    final Map<String, Object> object = getObjectParams(serverId, objectId);
    final List<Map<String, Object>> operations = (List<Map<String, Object>>)object
      .get("operations");
    if (operations == null) {
      throw new IllegalArgumentException(
        "MBean " + serverId + " " + objectId + " does not have any operations");
    } else {
      final List<String> operationNames = new ArrayList<>();
      for (final Map<String, Object> attribute : operations) {
        final String name = (String)attribute.get("operationName");
        if (name != null) {
          operationNames.add(name);
        }
      }
      return operationNames;
    }
  }

  /**
   * Get the description for all operations for an object on a server.
   *
   * @param serverId The name of the server.
   * @param objectId The name of the object.
   * @return The operations values.
   */
  public List<MBeanOperationInfo> getOperations(final String serverId, final String objectId) {
    final MBeanServerConnection connection = getConnection(serverId);
    try {
      final ObjectName objectName = getObjectName(serverId, objectId);
      final List<String> operationNames = getOperationNames(serverId, objectId);
      final MBeanInfo beanInfo = connection.getMBeanInfo(objectName);
      final List<MBeanOperationInfo> operations = new ArrayList<>();
      for (final MBeanOperationInfo operation : beanInfo.getOperations()) {
        final String name = operation.getName();
        if (operationNames.contains(name)) {
          operations.add(operation);
        }
      }
      return operations;
    } catch (final InstanceNotFoundException e) {
      throw new IllegalArgumentException("MBean not found " + serverId + " " + objectId);
    } catch (final MalformedObjectNameException e) {
      throw new IllegalArgumentException("MBean name not valid " + serverId + " " + objectId);
    } catch (final Throwable e) {
      return Exceptions.throwUncheckedException(e);
    }
  }

  /**
   * Get the configuration parameters for a server. Throws an
   * IllegalArgumentException if the server does not have a configuration.
   *
   * @param serverId The name of the server.
   * @return The server parameters.
   */
  private Map<String, Object> getServerParams(final String serverId) {
    final Map<String, Object> server = this.jmxServers.get(serverId);
    if (server == null) {
      throw new IllegalArgumentException("Server " + serverId + " not found");
    } else {
      return server;
    }
  }

  /**
   * Check to see if the object has been configured to have the specified
   * attribute.
   *
   * @param serverId The name of the server.
   * @param objectId The name of the object.
   * @param attributeId The name of the attribute.
   * @return True if the attribute exists.
   */
  public boolean hasAttribute(final String serverId, final String objectId,
    final String attributeId) {
    return getAttributeNames(serverId, objectId).contains(attributeId);
  }

  /**
   * Check to see if the object has been configured to have the specified
   * operation.
   *
   * @param serverId The name of the server.
   * @param objectId The name of the object.
   * @param operationId The name of the operation.
   * @return True if the operation exists.
   */
  public boolean hasOperation(final String serverId, final String objectId,
    final String operationId) {
    return getOperationNames(serverId, objectId).contains(operationId);
  }

  /**
   * Invoke the operation with the specified parameters.
   *
   * @param serverId The name of the server.
   * @param objectId The name of the object.
   * @param operationId The name of the operation.
   * @param parameters The parameters to pass to the operation.
   * @return the result of executing the operation.
   */
  public Object invokeOperation(final String serverId, final String objectId,
    final String operationId, final Map<String, String> parameters) {
    try {
      final MBeanOperationInfo operation = getOperation(serverId, objectId, operationId);
      final MBeanServerConnection connection = getConnection(serverId);
      final ObjectName objectName = getObjectName(serverId, objectId);
      final MBeanParameterInfo[] parameterInfos = operation.getSignature();
      final String[] signature = new String[parameterInfos.length];
      final Object[] values = new Object[parameterInfos.length];
      for (int i = 0; i < parameterInfos.length; i++) {
        final MBeanParameterInfo parameterInfo = parameterInfos[i];
        final String name = parameterInfo.getName();
        final String type = parameterInfo.getType();
        signature[i] = type;
        values[i] = parameters.get(name);
      }

      return connection.invoke(objectName, operation.getName(), values, signature);
    } catch (final InstanceNotFoundException e) {
      throw new IllegalArgumentException("MBean not found " + serverId + " " + objectId);
    } catch (final MalformedObjectNameException e) {
      throw new IllegalArgumentException("MBean name not valid " + serverId + " " + objectId);
    } catch (final Throwable e) {
      return Exceptions.throwUncheckedException(e);
    }
  }

  /**
   * Write the attribute for the attribute for an object on a server.
   *
   * @param mapWriter The writer to write the attributes to.
   * @param serverId The name of the server.
   * @param objectId The name of the object.
   * @param attributeId The name of the attribute.
   */
  public void writeAttribute(final MapWriter mapWriter, final String serverId,
    final String objectId, final String attributeId) {
    Object value;
    try {
      value = getAttribute(serverId, objectId, attributeId);
    } catch (final Throwable e) {
      value = e.getMessage();
    }
    writeAttribute(mapWriter, serverId, objectId, attributeId, value);
  }

  /**
   * Write the attribute for the attribute for an object on a server.
   *
   * @param mapWriter The writer to write the attributes to.
   * @param serverId The name of the server.
   * @param objectId The name of the object.
   * @param attributeId The name of the attribute.
   * @param value The value to write.
   */
  public void writeAttribute(final MapWriter mapWriter, final String serverId,
    final String objectId, final String attributeId, final Object value) {

    final Map<String, Object> attributeMap = new LinkedHashMap<>();
    attributeMap.put("serverId", serverId);
    attributeMap.put("objectId", objectId);
    attributeMap.put("attributeId", attributeId);
    attributeMap.put("value", value);
    mapWriter.write(attributeMap);
  }

  /**
   * Write the attributes for all servers and objects.
   *
   * @param mapWriter The writer to write the attributes to.
   */
  public void writeAttributes(final MapWriter mapWriter) {
    for (final String serverId : this.jmxServers.keySet()) {
      writeAttributes(mapWriter, serverId);
    }
  }

  /**
   * Write the attributes for all objects for a server.
   *
   * @param mapWriter The writer to write the attributes to.
   * @param serverId The name of the server.
   */
  public void writeAttributes(final MapWriter mapWriter, final String serverId) {
    final List<Map<String, Object>> objects = getObjectParams(serverId);
    for (final Map<String, Object> object : objects) {
      final String objectId = (String)object.get("objectLabel");
      writeAttributes(mapWriter, serverId, objectId);
    }
  }

  /**
   * Write the attributes for the objects on a server.
   *
   * @param mapWriter The writer to write the attributes to.
   * @param serverId The name of the server.
   * @param objectId The name of the object.
   */
  public void writeAttributes(final MapWriter mapWriter, final String serverId,
    final String objectId) {
    final String[] attributeNames = getAttributeNames(serverId, objectId).toArray(new String[0]);
    for (final String attributeId : attributeNames) {
      writeAttribute(mapWriter, serverId, objectId, attributeId);
    }
  }

  /**
   * Write the operation for the operation for an object on a server.
   *
   * @param mapWriter The writer to write the operation to.
   * @param serverId The name of the server.
   * @param objectId The name of the object.
   * @param operation The operation details.
   * @param returnValue The value returned from invoking the method.
   */
  public void writeOperation(final MapWriter mapWriter, final String serverId,
    final String objectId, final MBeanOperationInfo operation, final Object returnValue) {

    final Map<String, Object> attributeMap = new LinkedHashMap<>();
    attributeMap.put("serverId", serverId);
    attributeMap.put("objectId", objectId);
    attributeMap.put("operationId", operation.getName());
    attributeMap.put("returnType", operation.getReturnType());
    final MBeanParameterInfo[] parameters = operation.getSignature();
    final StringBuilder parameterSpec = new StringBuilder();
    if (parameters.length > 0) {
      for (final MBeanParameterInfo parameter : parameters) {
        final String name = parameter.getName();
        final String type = parameter.getType();
        parameterSpec.append(name).append(':').append(type).append(',');
      }
      parameterSpec.setLength(parameterSpec.length() - 1);
    }
    attributeMap.put("parameters", parameterSpec);
    attributeMap.put("returnValue", returnValue);
    mapWriter.write(attributeMap);
  }

  /**
   * Write the operation for the operation for an object on a server.
   *
   * @param mapWriter The writer to write the operation to.
   * @param serverId The name of the server.
   * @param objectId The name of the object.
   * @param operationId The name of the operations.
   */
  public void writeOperation(final MapWriter mapWriter, final String serverId,
    final String objectId, final String operationId) {
    final MBeanOperationInfo operation = getOperation(serverId, objectId, operationId);
    writeOperation(mapWriter, serverId, objectId, operation, null);
  }

  /**
   * Write the operation for the operation for an object on a server.
   *
   * @param mapWriter The writer to write the operation to.
   * @param serverId The name of the server.
   * @param objectId The name of the object.
   * @param operationId The name of the operations.
   * @param returnValue The value returned from invoking the method.
   */
  public void writeOperation(final MapWriter mapWriter, final String serverId,
    final String objectId, final String operationId, final Object returnValue) {
    final MBeanOperationInfo operation = getOperation(serverId, objectId, operationId);
    writeOperation(mapWriter, serverId, objectId, operation, returnValue);
  }

  /**
   * Write the operations for all servers and objects.
   *
   * @param mapWriter The writer to write the operations to.
   */
  public void writeOperations(final MapWriter mapWriter) {
    for (final String serverId : this.jmxServers.keySet()) {
      writeOperations(mapWriter, serverId);
    }
  }

  /**
   * Write the operations for all objects for a server.
   *
   * @param mapWriter The writer to write the operations to.
   * @param serverId The name of the server.
   */
  public void writeOperations(final MapWriter mapWriter, final String serverId) {
    final List<Map<String, Object>> objects = getObjectParams(serverId);
    for (final Map<String, Object> object : objects) {
      final String objectId = (String)object.get("objectLabel");
      try {
        writeOperations(mapWriter, serverId, objectId);
      } catch (final IllegalArgumentException e) {
      }
    }
  }

  /**
   * Write the operations for the objects on a server.
   *
   * @param mapWriter The writer to write the operations to.
   * @param serverId The name of the server.
   * @param objectId The name of the object.
   */
  public void writeOperations(final MapWriter mapWriter, final String serverId,
    final String objectId) {
    final List<MBeanOperationInfo> operations = getOperations(serverId, objectId);
    for (final MBeanOperationInfo operation : operations) {
      writeOperation(mapWriter, serverId, objectId, operation, null);
    }
  }
}
