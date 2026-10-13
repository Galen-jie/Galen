export const PET_TYPE = {
  DOG: 1,
  CAT: 2,
  OTHER: 3
}

export const PET_TYPE_TEXT = {
  [PET_TYPE.DOG]: '狗',
  [PET_TYPE.CAT]: '猫',
  [PET_TYPE.OTHER]: '其他'
}

export const PET_STATUS = {
  WAITING: 0,
  RESERVED: 1,
  ADOPTED: 2,
  OFFLINE: 3
}

export const PET_STATUS_TEXT = {
  [PET_STATUS.WAITING]: '待领养',
  [PET_STATUS.RESERVED]: '已预约',
  [PET_STATUS.ADOPTED]: '已领养',
  [PET_STATUS.OFFLINE]: '已下架'
}

export const USER_ROLE = {
  USER: 0,
  RESCUE_STATION: 1
}

export const USER_ROLE_TEXT = {
  [USER_ROLE.USER]: '普通用户',
  [USER_ROLE.RESCUE_STATION]: '救助站/志愿者'
}

export const ADOPTION_STATUS = {
  PENDING: 0,
  APPROVED: 1,
  REJECTED: 2,
  CANCELLED: 3
}

export const ADOPTION_STATUS_TEXT = {
  [ADOPTION_STATUS.PENDING]: '待审核',
  [ADOPTION_STATUS.APPROVED]: '已通过',
  [ADOPTION_STATUS.REJECTED]: '已拒绝',
  [ADOPTION_STATUS.CANCELLED]: '已取消'
}