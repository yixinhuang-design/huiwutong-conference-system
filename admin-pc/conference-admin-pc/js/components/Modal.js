/**
 * Vue 3 模态框组件 - 替代 Layui Layer
 * 
 * 使用方法:
 * 
 * 1. 在 HTML 中添加组件:
 * <Modal 
 *   :visible="showModal"
 *   title="确认操作"
 *   @confirm="handleConfirm"
 *   @cancel="handleCancel"
 * >
 *   <p>确定要执行此操作吗？</p>
 * </Modal>
 * 
 * 2. 在 Vue 组件中使用:
 * import Modal from './components/Modal.js';
 * 
 * export default {
 *   components: { Modal },
 *   data() {
 *     return {
 *       showModal: false
 *     }
 *   },
 *   methods: {
 *     openModal() {
 *       this.showModal = true;
 *     },
 *     handleConfirm() {
 *       console.log('已确认');
 *       this.showModal = false;
 *     },
 *     handleCancel() {
 *       console.log('已取消');
 *       this.showModal = false;
 *     }
 *   }
 * }
 */

const ModalTemplate = `
  <transition name="modal-fade">
    <div v-if="visible" class="modal-overlay" @click="handleOverlayClick">
      <div class="modal-content" @click.stop>
        <!-- 模态框头部 -->
        <div class="modal-header">
          <h2 class="modal-title">{{ title }}</h2>
          <button class="modal-close" @click="$emit('cancel')" aria-label="关闭">
            <i class="fas fa-times"></i>
          </button>
        </div>
        
        <!-- 模态框主体 -->
        <div class="modal-body">
          <slot></slot>
        </div>
        
        <!-- 模态框底部 -->
        <div v-if="showFooter" class="modal-footer">
          <button class="btn btn-secondary" @click="$emit('cancel')">
            {{ cancelText }}
          </button>
          <button class="btn btn-primary" @click="$emit('confirm')">
            {{ confirmText }}
          </button>
        </div>
      </div>
    </div>
  </transition>
`;

const ModalComponent = {
  name: 'Modal',
  template: ModalTemplate,
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    title: {
      type: String,
      default: '提示'
    },
    confirmText: {
      type: String,
      default: '确认'
    },
    cancelText: {
      type: String,
      default: '取消'
    },
    showFooter: {
      type: Boolean,
      default: true
    },
    closeOnOverlay: {
      type: Boolean,
      default: false
    }
  },
  emits: ['confirm', 'cancel'],
  methods: {
    handleOverlayClick() {
      if (this.closeOnOverlay) {
        this.$emit('cancel');
      }
    }
  }
};

// 如果是在浏览器环境中，导出组件
if (typeof module !== 'undefined' && module.exports) {
  module.exports = ModalComponent;
}
