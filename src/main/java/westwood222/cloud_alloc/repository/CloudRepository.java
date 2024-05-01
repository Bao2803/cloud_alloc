package westwood222.cloud_alloc.repository;

import westwood222.cloud_alloc.dto.CreateRequest;
import westwood222.cloud_alloc.dto.CreateResponse;
import westwood222.cloud_alloc.dto.DeleteRequest;
import westwood222.cloud_alloc.dto.DeleteResponse;
import westwood222.cloud_alloc.dto.ReadRequest;
import westwood222.cloud_alloc.dto.ReadResponse;
import westwood222.cloud_alloc.dto.UpdateRequest;
import westwood222.cloud_alloc.dto.UpdateResponse;

public interface CloudRepository {
    CreateResponse Create(CreateRequest request);

    ReadResponse Read(ReadRequest request);

    UpdateResponse Update(UpdateRequest request);

    DeleteResponse Delete(DeleteRequest request);
}
