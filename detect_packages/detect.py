def localize_objects(path):
    """
    Localize objects in the local image.

    Args:
    path: The path to the local file.
    """
    
    
    path = 'images/' + str(path)
    client = vision.ImageAnnotatorClient()

    with open(path, 'rb') as image_file:
        content = image_file.read()
    image = vision.Image(content=content)

    objects = client.object_localization(
        image=image).localized_object_annotations
    
    d = {}
    i = 0
    for object_ in objects:
        if object_.name in d.keys():
            if object_.score > d[object_.name]:
                d[object_.name] = object_.score
        else:
            d[object_.name] = object_.score

    s = {'result': 0}
    x = ["Box", "Packaged goods", "Shipping box"]


    #AUTO ML
    prediction_client = automl.PredictionServiceClient()

    # Get the full path of the model
    model_full_id = automl.AutoMlClient.model_path(project_id, "us-central1", model_id)

    file_path = 'images/' + str(path)
    # Read the file
    with open(file_path, "rb") as content_file:
        content = content_file.read()

    image = automl.Image(image_bytes=content)
    payload = automl.ExamplePayload(image=image)

    # params is additional domain-specific parameters
    # score_threshold is used to filter the result
    # https://cloud.google.com/automl/docs/reference/rpc/google.cloud.automl.v1#predictrequest
    params = {"score_threshold": "0.5"}

    request = automl.PredictRequest(name=model_full_id, payload=payload, params=params)

    response = prediction_client.predict(request=request)
    for result in response.payload:
        if result.display_name in ("packaged_goods", "packaged_goods2"):
            if 'Packaged goods' in s.keys():
                if s['Packaged goods'] > result.image_object_detection.score:
                    s['Packaged goods'] = result.image_object_detection.score
            else:
                s['Packaged goods'] = result.image_object_detection.score
    
    detected = 0
    for k, v in d.items():
        if k in x:
            if k in s.keys()
                if v > s[k]:
                    s[k] = v
            else:
                s[k] = v
                
        if s[k] > .5: #threshold
            detected = 1
        
    if detected == 1:
        s['result'] = 1

    return s 






#================================================================================
#     from google.cloud import vision
#     client = vision.ImageAnnotatorClient()

#     with open(path, 'rb') as image_file:
#         content = image_file.read()
#     image = vision.Image(content=content)

#     objects = client.object_localization(
#         image=image).localized_object_annotations

#     print('Number of objects found: {}'.format(len(objects)))
#     for object_ in objects:
#         print('\n{} (confidence: {})'.format(object_.name, object_.score))
#         print('Normalized bounding polygon vertices: ')
#         for vertex in object_.bounding_poly.normalized_vertices:
#             print(' - ({}, {})'.format(vertex.x, vertex.y))
            
    