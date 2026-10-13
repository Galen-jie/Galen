import { defineStore } from 'pinia'
import { getStorage, setStorage } from '@/utils/storage'

export const useCartStore = defineStore('cart', {
  state: () => ({
    cartItems: getStorage('cart') || []
  }),

  getters: {
    totalPrice: (state) => {
      return state.cartItems.reduce((total, item) => {
        return total + item.goodsPrice * item.quantity
      }, 0)
    },

    totalCount: (state) => {
      return state.cartItems.reduce((total, item) => {
        return total + item.quantity
      }, 0)
    }
  },

  actions: {
    addToCart(goods, quantity = 1) {
      const index = this.cartItems.findIndex(item => item.id === goods.id)

      if (index > -1) {
        this.cartItems[index].quantity += quantity
      } else {
        this.cartItems.push({
          ...goods,
          quantity
        })
      }

      this.saveCart()
    },

    removeCartItem(id) {
      this.cartItems = this.cartItems.filter(item => item.id !== id)
      this.saveCart()
    },

    updateQuantity(id, quantity) {
      const index = this.cartItems.findIndex(item => item.id === id)
      if (index > -1) {
        this.cartItems[index].quantity = quantity
        this.saveCart()
      }
    },

    clearCart() {
      this.cartItems = []
      this.saveCart()
    },

    saveCart() {
      setStorage('cart', this.cartItems)
    }
  }
})