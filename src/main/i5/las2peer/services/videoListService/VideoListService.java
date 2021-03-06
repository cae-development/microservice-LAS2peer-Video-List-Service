package i5.las2peer.services.videoListService;

import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mysql.jdbc.ResultSetMetaData;

import i5.las2peer.api.Service;
import i5.las2peer.restMapper.HttpResponse;
import i5.las2peer.restMapper.MediaType;
import i5.las2peer.restMapper.RESTMapper;
import i5.las2peer.restMapper.annotations.Version;
import i5.las2peer.security.Context;
import i5.las2peer.services.videoListService.database.DatabaseManager;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Contact;
import io.swagger.annotations.Info;
import io.swagger.annotations.License;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.jaxrs.Reader;
import io.swagger.models.Swagger;
import io.swagger.util.Json;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;


/**
 * 
 * LAS2peer Video List Service
 * 
 * This microservice was generated by the CAE (Community Application Editor). If you edit it, please
 * make sure to keep the general structure of the file and only add the body of the methods provided
 * in this main file. Private methods are also allowed, but any "deeper" functionality should be
 * outsourced to (imported) classes.
 * 
 */
@Path("videos")
@Version("0.1") // this annotation is used by the XML mapper
@Api
@SwaggerDefinition(info = @Info(title = "LAS2peer Video List Service", version = "0.1",
    description = "A LAS2peer microservice generated by the CAE.", termsOfService = "none",
    contact = @Contact(name = "Peter de Lange", email = "CAEAddress@gmail.com") ,
    license = @License(name = "BSD",
        url = "https://github.com/CAE-Community-Application-Editor/microservice-LAS2peer-Video-List-Service/blob/master/LICENSE.txt") ) )
public class VideoListService extends Service {


  /*
   * Database configuration
   */
  private String jdbcDriverClassName;
  private String jdbcLogin;
  private String jdbcPass;
  private String jdbcUrl;
  private String jdbcSchema;
  private DatabaseManager dbm;


  public VideoListService() {
    // read and set properties values
    setFieldValues();
    // instantiate a database manager to handle database connection pooling and credentials
    dbm = new DatabaseManager(jdbcDriverClassName, jdbcLogin, jdbcPass, jdbcUrl, jdbcSchema);
  }

  // //////////////////////////////////////////////////////////////////////////////////////
  // Service methods.
  // //////////////////////////////////////////////////////////////////////////////////////


  /**
   * 
   * getVideoList
   * 
   * 
   * @return HttpResponse
   * 
   */
  @GET
  @Path("/")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.TEXT_PLAIN)
  @ApiResponses(value = {
      @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "internalError"),
      @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "videoListAsJSONArray"),
      @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "noVideosExist")})
  @ApiOperation(value = "getVideoList", notes = "")
  public HttpResponse getVideoList() {
    String result = "";
    String columnName = "";
    String selectquery = "";
    int columnCount = 0;
    Connection conn = null;
    PreparedStatement stmnt = null;
    ResultSet rs = null;
    ResultSetMetaData rsmd = null;
    JSONObject ro = null;
    JSONArray array = new JSONArray();
    try {
      // get connection from connection pool
      conn = dbm.getConnection();
      selectquery = "SELECT * FROM videodetails;";
      // prepare statement
      stmnt = conn.prepareStatement(selectquery);

      // retrieve result set
      rs = stmnt.executeQuery();
      rsmd = (ResultSetMetaData) rs.getMetaData();
      columnCount = rsmd.getColumnCount();

      // process result set
      while (rs.next()) {
        ro = new JSONObject();
        for (int i = 1; i <= columnCount; i++) {
          result = rs.getString(i);
          columnName = rsmd.getColumnName(i);
          // setup resulting JSON Object
          ro.put(columnName, result);

        }
        array.add(ro);
      }
      if (array.isEmpty()) {
        String er = "No results";
        HttpResponse noVideosExist = new HttpResponse(er, HttpURLConnection.HTTP_NOT_FOUND);
        return noVideosExist;
      } else {
        // return HTTP Response on success
        HttpResponse videoListAsJSONArray =
            new HttpResponse(array.toJSONString(), HttpURLConnection.HTTP_OK);
        return videoListAsJSONArray;
      }
    } catch (Exception e) {
      String er = "Internal error: " + e.getMessage();
      HttpResponse internalError = new HttpResponse(er, HttpURLConnection.HTTP_INTERNAL_ERROR);
      return internalError;
    } finally {
      // free resources
      if (rs != null) {
        try {
          rs.close();
        } catch (Exception e) {
          Context.logError(this, e.getMessage());
          String er = "Internal error: " + e.getMessage();
          HttpResponse internalError = new HttpResponse(er, HttpURLConnection.HTTP_INTERNAL_ERROR);
          return internalError;
        }
      }
      if (stmnt != null) {
        try {
          stmnt.close();
        } catch (Exception e) {
          Context.logError(this, e.getMessage());
          String er = "Internal error: " + e.getMessage();
          HttpResponse internalError = new HttpResponse(er, HttpURLConnection.HTTP_INTERNAL_ERROR);
          return internalError;
        }
      }
      if (conn != null) {
        try {
          conn.close();
        } catch (Exception e) {
          Context.logError(this, e.getMessage());
          String er = "Internal error: " + e.getMessage();
          HttpResponse internalError = new HttpResponse(er, HttpURLConnection.HTTP_INTERNAL_ERROR);
          return internalError;
        }
      }
    }
  }


  // //////////////////////////////////////////////////////////////////////////////////////
  // Methods required by the LAS2peer framework.
  // //////////////////////////////////////////////////////////////////////////////////////


  /**
   * 
   * This method is needed for every RESTful application in LAS2peer. Please don't change.
   * 
   * @return the mapping
   * 
   */
  public String getRESTMapping() {
    String result = "";
    try {
      result = RESTMapper.getMethodsAsXML(this.getClass());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return result;
  }


  /**
   * 
   * Returns the API documentation of all annotated resources for purposes of Swagger documentation.
   * 
   * @return The resource's documentation
   * 
   */
  @GET
  @Path("/swagger.json")
  @Produces(MediaType.APPLICATION_JSON)
  public HttpResponse getSwaggerJSON() {
    Swagger swagger = new Reader(new Swagger()).read(this.getClass());
    if (swagger == null) {
      return new HttpResponse("Swagger API declaration not available!",
          HttpURLConnection.HTTP_NOT_FOUND);
    }
    try {
      return new HttpResponse(Json.mapper().writeValueAsString(swagger), HttpURLConnection.HTTP_OK);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return new HttpResponse(e.getMessage(), HttpURLConnection.HTTP_INTERNAL_ERROR);
    }
  }

}
