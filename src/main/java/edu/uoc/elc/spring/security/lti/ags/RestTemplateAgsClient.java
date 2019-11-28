package edu.uoc.elc.spring.security.lti.ags;

import edu.uoc.elc.spring.security.lti.utils.QueryBuilder;
import edu.uoc.lti.ags.AgsClient;
import edu.uoc.lti.ags.LineItem;
import edu.uoc.lti.ags.Result;
import edu.uoc.lti.ags.Score;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.util.StringUtils;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.AbstractMap;
import java.util.List;

/**
 * @author Xavi Aracil <xaracil@uoc.edu>
 */
@RequiredArgsConstructor(staticName = "of")
public class RestTemplateAgsClient implements AgsClient {
	private final OAuth2RestOperations restContainerTemplate;
	private final OAuth2RestOperations restItemTemplate;
	private final URI lineItemsUri;
	private final boolean canReadGrades;
	private final boolean canReadLineItems;
	private final boolean canManageLineItems;
	private final boolean canScore;

	public boolean canReadGrades() {
		return canReadGrades;
	}

	public boolean canReadLineItems() {
		return canReadLineItems;
	}

	public boolean canManageLineItems() {
		return canManageLineItems;
	}

	public boolean canScore() {
		return canScore;
	}

	/**
	 * Get the line items of the platform
	 * @param limit restricts the maximum number of items to be returned. If null doesn't apply this restriction.
	 * @param page indicates the offset for which this page should start containing items. Can be null.
	 * @param resourceLinkId limit the line items returned to only those which have been associated with the specified tool platform's resource link. Can be null.
	 * @param tag limit the line items returned to only those which have been associated with the specified tag. Can be null.
	 * @param resourceId limit the line items returned to only those which have been associated with the specified tool resource ID. Can be null.
	 * @return all the line items associated to tool in the specified learning context. Results may be broken in multiple pages in particular if a limit parameter is present.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<LineItem> getLineItems(Integer limit, Integer page, String resourceLinkId, String tag, String resourceId) {
		if (!canReadLineItems()) {
			throw new MethodNotAllowedException("GET", null);
		}

		String query;
		try {

			query = QueryBuilder.of(new AbstractMap.SimpleImmutableEntry("limit", limit),
							new AbstractMap.SimpleImmutableEntry("page", page),
							new AbstractMap.SimpleImmutableEntry("resourceLinkId", resourceLinkId),
							new AbstractMap.SimpleImmutableEntry("tag", tag),
							new AbstractMap.SimpleImmutableEntry("resourceId", resourceId));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

		String uri = UriComponentsBuilder.fromUri(lineItemsUri).query(query).build().toUriString();
		ResponseEntity<List<LineItem>> responseEntity = restContainerTemplate.exchange(uri,
						HttpMethod.GET,
						null,
						new ParameterizedTypeReference<List<LineItem>>() {});
		return responseEntity.getBody();
	}

	/**
	 * Add a new line item; the created line item is returned with the url, scores and results endpoints (assuming the client has the enabled capabilities for those)
	 * @param lineItem the LineItem to create
	 * @return the LineItem created
	 */
	@Override
	public LineItem createLineItem(LineItem lineItem) {
		if (!canManageLineItems()) {
			throw new MethodNotAllowedException("POST", null);
		}
		final ResponseEntity<LineItem> responseEntity = restItemTemplate.postForEntity(lineItemsUri, lineItem, LineItem.class);
		return responseEntity.getBody();
	}

	/**
	 * Returns the current value for the line item.
	 * @param id the id of the line item.
	 * @return the line item of the given id
	 */
	@Override
	public LineItem getLineItem(String id) {
		if (!canReadLineItems()) {
			throw new MethodNotAllowedException("GET", null);
		}

		return restItemTemplate.getForObject(id, LineItem.class);
	}

	/**
	 * Updates the line item
	 * @param id Id of the line item.
	 * @param lineItem the updated line item.
	 * @return the updated line item.
	 */
	@Override
	public LineItem updateLineItem(String id, LineItem lineItem) {
		if (!canManageLineItems()) {
			throw new MethodNotAllowedException("PUT", null);
		}

		ResponseEntity<LineItem> responseEntity = restItemTemplate.exchange(id,
						HttpMethod.PUT,
						new HttpEntity<>(lineItem),
						new ParameterizedTypeReference<LineItem>() {});

		return responseEntity.getBody();
	}

	/**
	 * Removes the line item. While no more associated with the tool, the tool platform may decide to keep the line item around (unassociated)
	 * @param id Id of the line item.
	 */
	@Override
	public void deleteLineItem(String id) {
		if (!canManageLineItems()) {
			throw new MethodNotAllowedException("DELETE", null);
		}

		restItemTemplate.delete(id);
	}

	/**
	 * Returns all the results for the line item (results for all the students in the current context for this line item) Results may be broken in multiple pages in particular if a limit parameter is present.
	 * @param id Id of the line item.
	 * @param limit restricts the maximum number of items to be returned. The response may be further constrained. If null doesn't apply this restriction.
	 * @param page indicates the offset for which this page should start containing items. Used exclusively by the nextPage URL. Can be null.
	 * @param userId limit the line items returned to only those which have been associated with this user. Results must contain at most one result.
	 * @return all the results for the line item
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Result> getLineItemResults(String id, Integer limit, Integer page, String userId) {
		if (!canReadGrades()) {
			throw new MethodNotAllowedException("GET", null);
		}
		String query;
		try {
			query = QueryBuilder.of(new AbstractMap.SimpleImmutableEntry("limit", limit),
							new AbstractMap.SimpleImmutableEntry("page", page),
							new AbstractMap.SimpleImmutableEntry("userId", userId));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

		String path = String.format( "%s/results", id);
		String uri = path + (!StringUtils.isEmpty(query) ? "?" + query : "");

		ResponseEntity<List<Result>> responseEntity = restContainerTemplate.exchange(uri,
						HttpMethod.GET,
						null,
						new ParameterizedTypeReference<List<Result>>() {});
		return responseEntity.getBody();
	}

	/**
	 * Publishes a score update. Tool platform may decide to change the result value based on the updated score.
	 * @param lineItemId Id of the line item
	 * @param score the score to update
	 * @return true if the score was updated. false otherwise
	 */
	@Override
	public boolean score(String lineItemId, Score score) {
		if (!canScore()) {
			throw new MethodNotAllowedException("POST", null);
		}
		String uri = String.format( "%s/scores", lineItemId);

		restItemTemplate.postForLocation(uri, score);
		return true;
	}
}