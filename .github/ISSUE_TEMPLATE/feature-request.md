---
name: Feature request
about: Suggest new feature
title: ''
labels: api, feature
assignees: ''

---

# Feature name
Describe here your feature in few words.

## Request
Describe here how must be structured the request:
- HTTP method
- HTTP headers
- Body structure

Only accepted `Content-Type` are `application/json` and `multipart/form-data`.

## Response
Describe here how must be structured the response:
- HTTP status code
- HTTP headers
- Body structure

## Example
**Request**
Describe here an example of request.
```
POST /path
Content-Type: application/json
```
```json
{
  "foo": "bar"
}
```

**Response**
Describe the response of the previous example of request.
```
201 Created
Location: /path/id
```
```json
```
