import request from '@/utils/request'

export function uploadFile(file, businessType) {
  const formData = new FormData()
  formData.append('file', file)
  if (businessType) {
    formData.append('businessType', businessType)
  }

  return request({
    url: '/file/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function uploadFiles(files, businessType) {
  const formData = new FormData()
  files.forEach(file => {
    formData.append('files', file)
  })
  if (businessType) {
    formData.append('businessType', businessType)
  }

  return request({
    url: '/file/upload/batch',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function deleteFile(fileUrl) {
  return request({
    url: '/file',
    method: 'delete',
    params: {
      fileUrl
    }
  })
}